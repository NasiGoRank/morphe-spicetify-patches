# Original-order playback experiment (v1.2.1-alpha.3)

## Background

A physical Spotify Android Free device running the v1.2.1-alpha.2 probe returned:

- `SetOptions(shuffling_context=false)`: HTTP 200, **player FORBIDDEN (1)**, Shuffle remained ON.
- `SetShufflingContext(false)`: HTTP 200, **player FORBIDDEN (1)**, Shuffle remained ON.
- Player bridge connected. The `mft/force_shuffle` suppression wasn't observed.

This rules out repeated toggle RPCs as a promising fix for this specific playback session. It does **not** definitively locate the check in the backend or the native player.

## Different approach

The new opt-in experiment reuses the repository's existing `Esperanto.playOrder` builder, which constructs a single `ContextPlayer/Play` request containing:

1. A `Context` with track URIs listed in the **original order** returned by Spotify's playlist/album metadata service.
2. `PreparePlayOptions.skip_to` pointing at track index zero.
3. `player_options_override.shuffling_context = false`.

This is **not** another `SetOptions` or `SetShufflingContext` command. The player may still reject it with `FORBIDDEN (1)`, or accept it but ignore/reorder the provided tracks. No undocumented entitlement claim is made.

### How to test

1. Add Morphe remote source `https://raw.githubusercontent.com/NasiGoRank/morphe-spicetify-patches/refs/heads/experiment/ordered-playback-probe/patches-bundle.json`.
2. Confirm bundle version **1.2.1-alpha.3**, target Spotify **9.1.80.2221 ARM64 APKM** and enable *Experimental app versions*.
3. Select **Playback identity and Shuffle probe (experimental)**, but **do not select** `Request Shuffle OFF (experimental)`. Disable the previous source to avoid mixing selectors.
4. Start Spotify, open a regular playlist with a manageable number of songs, and let it play long enough to populate the state stream. Prefer a test playlist, not something you're relying on during travel.
5. Open **Spicetify → Home and navigation → Playback diagnostics**, then press **Play playlist in original order (experimental)**. **This action can restart the playlist at the FIRST SONG if Spotify accepts it.**
6. After the result loads, press **Refresh diagnostic results** and **Copy redacted results**. Share the ordered playback result text and check the order of two or more songs in Spotify.
7. Do not interpret Shuffle button still showing ON as decisive: the explicit request could have been accepted or rejected independently. Judge the actual playback order and returned code.

The probe supports playlists, albums, and Liked Songs, capped at 1500 tracks; it does not modify playlist content or upload any data. On failure it leaves playback unchanged, subject to Spotify's own behavior. The report excludes track URIs, playlist titles, credentials, and account/device identifiers.

### What the results mean

- **Play rejected: FORBIDDEN (1)**: the explicit ordered-playback route also hit Spotify's restriction. Do not repeat it automatically.
- **Play reported OK**: check whether the next songs follow the playlist order. A code zero alone proves nothing about ongoing shuffle.
- **Transport/metadata error**: diagnose bridge availability, playlist metadata visibility, and compatibility separately.

This does **not** spoof Desktop identity. Spotify Connect device category alone does not establish playback entitlement. The [official Web API Start/Resume Playback](https://developer.spotify.com/documentation/web-api/reference/start-a-users-playback) and [Add to Queue](https://developer.spotify.com/documentation/web-api/reference/add-to-queue) are Premium-only; the request in this experiment uses an internal Android Esperanto route, so official Web API behavior is only context, not proof of its outcome.

No physical-device validation of v1.2.1-alpha.3 has been performed yet.
