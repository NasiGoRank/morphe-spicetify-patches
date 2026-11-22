# Playback-safety recovery preview

**Scope:** Spotify Android 9.1.80.2221 ARM64 (APKM), fork `NasiGoRank/morphe-spicetify-patches`.

## Reported issue

On both the `main v1.0.0` and `premium-popups-v1.0.1-alpha.1` bundles, playback sometimes rapidly skips every track and does not play selected songs. This is **not yet attributed to the patch**. Other Spotify versions (including unmodified mobile clients) have also had similar defects.

Both bundles share the optional `Disable audio ad breaks (experimental)` patch. It periodically sends `spotify.ads.esperanto.proto.Settings/updateSlotEnabled` with `enabled=false` for the `stream` and `preroll` slots.

### Confirmed safety issue

The previous OFF switch stopped future disable requests but did **not** restore the slots. Thus, users could not rely on toggling OFF to immediately undo the experimental override.

## Recovery change

- One-time migration turns `Disable audio ad breaks (experimental)` **OFF** on first launch of this safety build even if it had been selected and enabled under the older release.
- When OFF, the player bridge requests `updateSlotEnabled(enabled=true)` for both `stream` and `preroll` slots. This is an attempt to restore Spotify's normal behavior; server acceptance isn't guaranteed.
- When ON (explicitly opted in again), the old experimental behavior remains possible but may reproduce the fault.
- Does **not** change Shuffle, account capabilities, Premium popups, or other player controls. The main branch is untouched until an actual device test.

## Test plan

1. Before re-patching, turn `Disable audio ad breaks` OFF via **Spicetify > Ads** and force-stop Spotify. Try playing music. If the bug persists, proceed.
2. For a clean comparison, patch a verified Spotify 9.1.80.2221 ARM64/APKM using this source:
   `https://raw.githubusercontent.com/NasiGoRank/morphe-spicetify-patches/refs/heads/hotfix/audio-ad-playback-safety/patches-bundle.json`.
3. Choose **Disable audio ad breaks (experimental)** only if you need the restore behavior to run. On first app launch, it is reset OFF and restores both ad slots. Avoid checking `Hide player ad cards` and `Spicetify extensions` for this baseline test. Choose a harmless patch such as `Clean sharing links` if you do not need ad-slot restoration.
4. Restart Spotify and try a direct song, Next, and a playlist.
5. If it works, enable the audio ad patch **only if you specifically want to reproduce the fault**, then observe whether rapid skipping starts again. Turn it OFF afterward; this build sends restoration requests.
6. If disabling all ads-related and Spicetify extension patches still skips tracks, test the official Spotify Play Store app for the same account/network (after backing up any needed app data). A normal client exhibiting the same issue would point away from this patch.
7. Optional Windows ADB diagnostics:

```powershell
adb logcat -c
# Reproduce playing a track
adb logcat -d -v time -s Spicetify:V > spotify-bridge.log
Select-String -Path .\spotify-bridge.log -Pattern "Audio slot|player bridge|refused|failed"
```

Avoid publicly sharing the full logcat: it may contain device details or unrelated account information.

**Interpreting results:** If playback resumes after turning OFF/restoring slots, audio ad control becomes a leading suspect but still isn't proven as the only cause. If Spotify continues skipping with every experimental patch off, troubleshoot client version, account playback entitlement, and network next.

The patch remains experimental. Do not promote to main until the playback behavior is confirmed.
