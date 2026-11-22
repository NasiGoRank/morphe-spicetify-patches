# Experimental audio ad breaks and Shuffle OFF

This branch adds **two opt-in Morphe patches** for **Spotify Android 9.1.80.2221 / arm64-v8a**, reusing the already verified `playerBridgePatch` hook.

- **Disable audio ad breaks (experimental)**: sends `spotify.ads.esperanto.proto.Settings/updateSlotEnabled` with `enabled=false` for `stream` and `preroll`, initially and periodically while its switch is enabled. Inspired by [Pablobaro's GPL-3.0 patch](https://github.com/pablobaro/pablobaro-morphe-patches/blob/main/patches/src/main/kotlin/app/pablobaro/patches/spotify/ads/DisableAdBreaksPatch.kt) and [Adblockify's MIT-licensed desktop implementation](https://github.com/rxri/spicetify-extensions/blob/main/adblock/adblock.ts). The code here is newly written, with original upstream licenses preserved in their repositories.
- **Request Shuffle OFF (experimental)**: sends `spotify.player.esperanto.proto.ContextPlayer/SetOptions` with `shuffling_context=false` and re-requests when playback context changes. It does *not* change user entitlement data.

Both patches are off by default in Morphe patch selection. Once selected, their controls appear under **Spicetify → Ads** or **Home and navigation**; their runtime toggles default to enabled. Turning off the audio-ad option stops further requests; restart Spotify to restore normal ad-slot handling.

## Limitations

**No runtime test with Spotify 9.1.80.2221, Morphe Patcher 2.5.0, or Android 16 has been completed.** CI can validate compilation and protobuf request bytes but not server acceptance or real playback.

Spotify may reject, ignore, reset, or rate-limit either kind of request. An API success response does not prove ads were suppressed or shuffle remained off. Free accounts may be unable to disable shuffle; this patch is a best-effort request, not a Premium unlock. The app may refuse playback on modified signatures. Avoid uninstalling your working Spotify installation before confirming a replacement works.

Test one patch at a time, then together. Check login, playlist order, audio ad breaks, background playback, Android notifications, and Spotify Connect. Review logcat warnings tagged `Spicetify` if features fail.

Do not include proprietary Spotify APKs, user data, or signing keys in this repository.
