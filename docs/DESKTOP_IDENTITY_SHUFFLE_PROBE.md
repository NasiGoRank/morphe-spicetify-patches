# Device Identity + Shuffle Protocol Probe (Experimental)

Target: Spotify Android **9.1.80.2221 ARM64/APKM**, Morphe, isolated opt-in patch.

## Why not spoof "computer" immediately?

Spotify Connect officially reports devices using categories such as computer, smartphone, or speaker, but a Connect device **type is not a subscription or playback entitlement**. A fake `device_type=computer` alone may not enable Desktop-like Shuffle OFF and could break Connect. The Android client registration field and any linked native capability negotiation are not verified for this exact APK. We will **not** change them without a verified hook and a controlled result.

The old `Request Shuffle OFF (experimental)` patch sent `ContextPlayer/SetOptions` without verifying the live player state. The new probe tests the behavior, not just whether an HTTP request receives 200.

## What the opt-in patch does

- Uses the **existing, previously build-tested Cosmos bridge**; does not require a new unknown obfuscated hook.
- Observes `ContextPlayerState.options.shuffling_context` (17 -> 1).
- Observes **presence only** of the `mft/force_shuffle` suppression (19). Other suppression strings, track URLs, account/device IDs, and auth tokens are not copied into the report.
- On a **manual tap** in **Spicetify → Home and navigation → Playback diagnostics**, first sends `SetOptions(shuffling_context=false)`, records its RPC result and observed state, then sends `SetShufflingContext(false)` and records that result and state.
- Displays and copies a redacted report. Results may be "unknown" if the state stream is missing. A response with no error does **not** prove the player accepted the change.
- Source verification checks protobuf field numbers to fail closed if Spotify's layout differs. This branch does **not** spoof account identity, register as Desktop, modify client capabilities, or unlock Premium.

**Important:** Do **not** enable the previous automatic "Request Shuffle OFF" patch during the probe. It changes the same state and invalidates the comparison. The probe prevents running while that option is enabled.

## Test procedure

1. Install the *new preview bundle* from this branch as its own remote source. Disable the previous source, to avoid accidental mixes.
2. When patching, select **Playback identity and Shuffle probe (experimental)**. Leave **Request Shuffle OFF (experimental)** unselected; keep **Disable audio ad breaks** unselected for this controlled playback test too.
3. Open Spotify, start a normal playlist, and visit **Spicetify → Home and navigation → Playback diagnostics**.
4. Note the baseline **Live shuffle** and **mft/force_shuffle** indicator.
5. Tap **Run Shuffle protocol test**. After a few seconds tap **Refresh diagnostic results**, then **Copy redacted results**.
6. Verify song order in Spotify independently. The test never changes the appearance or entitlement of the Shuffle button.
7. Share the *redacted* report, not a raw Spotify logcat dump. Never post tokens, full API response payloads, or Spotify account details.

## Comparing the actual Connect type

To compare the Connect metadata from the PC and Android **without spoofing**, use Spotify's official [Get Available Devices](https://developer.spotify.com/documentation/web-api/reference/get-a-users-available-devices) endpoint with your own authorized token (scope: `user-read-playback-state`). This may require applicable Spotify developer app access. **Do not share the token.**

Save the two authorized JSON device-list responses locally as `desktop.json` and `android.json`, with the PC / phone active respectively, then run:

```bash
python3 scripts/compare-connect-devices.py --desktop desktop.json --android android.json
```

Only the reported device **type** and non-identifying status flags are printed, not IDs, names, or tokens.

This verifies Desktop vs smartphone reporting but **not** whether Spotify uses it to grant playback features. Spotify's [official Web API Toggle Playback Shuffle](https://developer.spotify.com/documentation/web-api/reference/toggle-shuffle-for-users-playback) states that it works only for Premium users.

## Possible outcomes

- Both RPCs fail: client cannot reach the method, transport breaks, or service rejects it.
- RPC 200 but player still reports ON: request accepted at transport, yet Shuffle is enforced or reset elsewhere.
- One RPC reports OFF in state: one method works; we can investigate a more targeted patch and UI handling.
- `mft/force_shuffle` present: a potential enforcement signal; not proof it alone controls the restriction.
- Spotify Connect reports `computer` on Desktop: expected, not evidence that spoofing alone grants Desktop features.

No real Android playback validation has occurred yet. All code in this branch must be considered experimental.
