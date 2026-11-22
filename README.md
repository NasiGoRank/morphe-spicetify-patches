# Spicetify Android patches

Spotify Android customizations for use with [Morphe](https://morphe.software/).
This repository publishes patch source and bundles, not Spotify APKs.

<!-- prettier-ignore -->
> [!NOTE]
> These patches target Spotify 9.1.80.2221, ARM64. Other Spotify versions are
> not supported.

[**➕ Add Spicetify to Morphe**](https://morphe.software/add-source?github=NasiGoRank/morphe-spicetify-patches/tree/main)

Open this link on Android with Morphe Manager installed to add the stable
source. Releases are published from `main`.

## Patches

Clean sharing is enabled by default; the other patches are optional. Choose
theme colors, Home pins, ad and Premium-tab hiding, and server files in
Spotify's Spicetify settings after installation.

Server files stream from an HTTPS WebDAV folder or a Jellyfin library. They
also add server albums and artists to Your Library, after
Spotify's own items, with a filter chip named after the provider (for example
**Jellyfin**) that shows only server items. Tapping a server album opens it and
plays it in order through Spotify's player; tapping an artist opens the artist.
Playback needs Spotify's **Local audio files** setting (Settings → Apps and
devices). The last completed scan is saved on the device, so server items are
available as soon as Spotify starts while a fresh scan runs. This was tested on a
Pixel 8 with a 35,000-track Jellyfin library.

| Patch | Default | Behavior |
| --- | --- | --- |
| Clean sharing links | Enabled | Removes `si`, `pi`, and known `utm_*` parameters from `open.spotify.com` links. Preserves timestamps, context, other parameters, and fragments. |
| Theme colors | Disabled | Choose a theme such as OLED, Midnight, or Nord in Spicetify settings, apply a community theme's colors and background image from the Spicetify Marketplace, paste a desktop Spicetify theme's `color.ini`, or pick background, surface, and accent colors yourself, then restart Spotify when it offers. Requires Android 11 or later. Hardcoded colors and some screens keep Spotify's colors. |
| Pin shortcuts on Home | Disabled | Pins playlists, albums or Liked Songs from your library first on Home, in the order you pick them. Pins show as Spotify's own shortcut tiles, even when Spotify's Home leaves them out, and **Show only my pins** hides Spotify's other shortcuts. Configure pins in Spotify's Spicetify settings, then restart Spotify. |
| Spicetify extensions | Disabled | Android versions of Spicetify extensions, each off until you turn it on in the Spicetify Marketplace's **Extensions** tab. **Trash Bin**: throw a song or an artist away from its **...** menu, and Spotify skips that song, or the artist's songs, whenever they play, as the desktop extension does. The trash list exports and imports in the desktop format. **Hide podcasts**: hides podcasts and episodes on Home and in Search, and their filters there and in Your Library, as the desktop extension does. Its **Also hide audiobooks** switch, on by default, hides audiobooks the same way. **Play a random song**: plays one song from all of Spotify, found with a Web API search, or from your library, from a **Random** pill next to **All** on Home or from its sheet in Spicetify settings. **Shuffle+**: plays a playlist, album, or Liked Songs in a truly random order, as the desktop extension does. Long-press the shuffle button in Now Playing, where a tap still toggles Spotify's shuffle, choose **Shuffle+ this playlist** in the **...** menu of a playlist or Liked Songs, even with nothing playing, or use its sheet in Spicetify settings. |
| Local files from a server | Disabled | Streams an HTTPS WebDAV folder or Jellyfin library into Local Files and Your Library, with its own filter chip. Requires Android 8 or later, byte-range support, and Spotify's Local audio files setting; configure the server in Spotify's Spicetify settings. Not available for root mount installs, because its track provider and server browser must be in the manifest. |

<!-- PATCHES_START EXPANDED -->
> **[v1.0.1](https://github.com/NasiGoRank/morphe-spicetify-patches/releases/tag/v1.0.1)**&nbsp;&nbsp;•&nbsp;&nbsp;`main`&nbsp;&nbsp;•&nbsp;&nbsp;9 patches total
<details open>
<summary>📦 Spotify&nbsp;&nbsp;•&nbsp;&nbsp;9 patches</summary>
<br>

**🎯 Supported versions:**

| 🧪&nbsp;9.1.80.2221 |
| :---: |
| Experimental Android customization patches; runtime compatibility is still being verified. |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Clean sharing links](#clean-sharing-links) | Removes sharing identifiers and marketing parameters from open.spotify.com links. Keeps playback timestamps, context, and other parameters. |  |
| [Disable audio ad breaks (experimental)](#disable-audio-ad-breaks-experimental) | Requests disabling stream and preroll audio ad slots. Spotify may refuse or ignore the request; does not unlock Premium or guarantee ad-free playback. |  |
| [Hide Home and Browse ads](#hide-home-and-browse-ads) | Hides image and video brand-ad sections on Home and Browse. Does not suppress audio ads, player ads, or upgrade prompts. Experimental. |  |
| [Hide Premium tab](#hide-premium-tab) | Hides the Premium navigation tab. Change this in Spicetify settings, then restart Spotify. Does not change your subscription or remove other ads. |  |
| [Hide player ad cards](#hide-player-ad-cards) | Hides image brand-ad cards and embedded ad pages in Now Playing. Does not suppress audio ads or other player overlays. Experimental. |  |
| [Local files from a server](#local-files-from-a-server) | Streams audio from an HTTPS WebDAV folder or Jellyfin music library into Local Files and Your Library. Configure the server in Spicetify settings; playback needs Spotify's Local audio files setting. Experimental; requires byte-range support. Not available for root mount installs. |  |
| [Pin shortcuts on Home](#pin-shortcuts-on-home) | Pick playlists, albums or Liked Songs to pin first on Home, or turn on Show only my pins to hide Spotify's other shortcuts. Pins are saved on this device; restart Spotify after changing them. |  |
| [Spicetify extensions](#spicetify-extensions) | Adds Android versions of Spicetify extensions to the Spicetify Marketplace's Extensions tab, in Spicetify settings, each off until you turn it on. Trash Bin skips the songs and artists you throw away from their menus. Play a random song plays one from all of Spotify or your library, from a Random pill on Home. Shuffle+ plays a playlist, album or Liked Songs in a truly random order when you long-press the shuffle button in Now Playing, choose Shuffle+ this playlist in a playlist's menu, or use its sheet. Hide podcasts hides podcasts and episodes on Home and in Search, and their filters there and in Your Library, and audiobooks unless you turn that off. |  |
| [Theme colors](#theme-colors) | Choose a theme, such as OLED, or your own colors in Spicetify settings. Requires Android 11 or later. Some screens and hardcoded colors keep Spotify's colors. |  |

</details>

<!-- PATCHES_END -->

## Morphe 2.x settings ABI patch failure

If Morphe reports `Spotify settings ABI changed: Lcom/spotify/music/SpotifyApplication;`
while using **Spicetify Android patches 1.2.0**, it is loading the older
**upstream release (2026-10-08)**. That release predates
[the ABI digest fix (PR #16)](https://github.com/spicetify/morphe-patches/pull/16).
The fork's source includes the fix, which hashes the *semantic DEX class
description* instead of version-dependent DEX writer bytes.

1. In **Morphe Manager > Sources**, disable/remove the old
   `spicetify/morphe-patches` source. Do not mix its patches with this fork's bundle.
2. Add this fork using the button above or the raw source URL below. Update the
   source and verify that the selected bundle comes from **NasiGoRank**, not the
   upstream **Spicetify Android patches 1.2.0**.
3. Select an **original, unmodified** Spotify **9.1.80.2221 ARM64**
   APK/APKM (version code `145767611`). Do not repatch an already patched APK.
4. For your initial test, select **only Clean sharing links**. Confirm the app
   launches, logs in, loads playlists, plays tracks, advances and seeks normally.
   Add other patches one at a time afterward. Keep **Spicetify extensions**
   (including Shuffle+) and the experimental audio-ad patch disabled initially.

If the error still occurs *with the fork bundle*, check the selected APK's
SHA-256 against the verified upstream testing fixture
`f4826f9a9148b15d4a7adeb10a01603a6eca2ff29d16c9d59a320439b4ff17da`.
A different hash is not necessarily malicious, but it is not the exact fixture
verified by upstream PR #16. Patcher 2.5.0 and physical-device playback have
not yet been verified for this fork.

## Install

Releases for this fork are on the [releases page](https://github.com/NasiGoRank/morphe-spicetify-patches/releases).
This fork's ABI-compatible build has not yet been validated for audible playback on Morphe Patcher 2.5.0; a successful bundle build does not guarantee playback.

A patched APK uses a different signing certificate from stock Spotify. Installing it with
the same package name requires removing stock Spotify first, which removes
its local app data and downloads. Keep Manager's signing key for future
updates; a different key requires another uninstall.

Use the **Add Spicetify to Morphe** link above, then confirm the source in
Manager. If you already added this repository manually, keep that source
instead of adding it again. To add it manually and patch Spotify:

1. Open **Sources**, select **Add**, and choose **Remote**.
2. Paste the following source URL, then select **Add**.

   ```text
   https://raw.githubusercontent.com/NasiGoRank/morphe-spicetify-patches/refs/heads/main/patches-bundle.json
   ```

   If you previously installed `spicetify/morphe-patches` as a Morphe source, disable that source before selecting patches from this fork. The official 1.2.0 bundle predates the ABI fix, and selecting it again reproduces the error.

3. Expand **Spicetify Android patches** and enable **Experimental app versions**.
4. Return to the app list. Spotify appears with the target version
   `9.1.80.2221`, ARM64 build `145767611`.
5. Optional: To add theme colors, Home pins, or server files, open
   **Settings > Advanced** and enable
   **Expert mode** before selecting Spotify. The default flow applies only
   **Clean sharing links**.
6. Select **Spotify**, choose **No, I already have an APK**, and select your
   original APK or split-APK archive. To use Android's system picker without
   granting **All files access**, turn off **Settings > System > Custom file
   picker** in Manager first.
7. Read the experimental-support notice and select **Proceed anyway** if you
   want to test this build. In Expert mode, select the optional patches you
   want, then select **Proceed to patching**.
8. Wait for **Patching complete**, then select **Install**. If Manager reports
   a certificate conflict, uninstall the existing app only after accepting
   the data loss described above. Confirm installation in Android's dialog.
9. Open Spotify from Android's app launcher. Sign in if needed.

Keep your own stock APK or split-APK archive for patching; the repository does
not distribute Spotify.

## Change settings in Spotify

Patched Spotify has a **Spicetify** row in its settings.

1. Open your profile menu, then **Settings and privacy**.
2. Scroll down and select **Spicetify**, just above **Log out**.
3. Turn **Clean sharing links** on or off. The next share uses your choice
   immediately. The setting survives restarting Spotify.

Only installed patches appear here, grouped by category. With **Theme colors**
installed, **Appearance** lists themes, including OLED and, on Android 12 or
later, Material You, and a **Custom** option for picking background, surface,
and accent colors. **Spicetify Marketplace** lists the community themes the
desktop Marketplace offers, most stars first, with previews and search. Tap one
to download its `color.ini` and choose a color scheme. Its colors apply, and
so does the background image it shows on desktop when its scripts or
`user.css` name one, as Hazy's and CyberNight's do. The list is kept for six
hours, and **Refresh** reloads it from GitHub. **Galaxy V2**, pinned at the
top, brings Galaxy's colors and background image. A background image shows
behind Spotify's main screen, whose pages turn see-through over it, and
**Appearance** offers **Blur background image** while one is in use.
**Paste a Spicetify theme** takes a desktop theme's
`color.ini`, or CSS with `--spice-*` colors, and lets you choose its color
scheme and an accent key such as Catppuccin's `mauve`. Settings that Spotify
reads at startup, such as themes, offer to restart Spotify for you. Home pins and server files have their own
controls here when installed.

With **Spicetify extensions** installed, the Spicetify page lists the
extensions that are on, each with its latest status. Tap one for its switch
and its own controls, such as Trash Bin's **Export**, **Import** and
**Clear**. **Spicetify Marketplace** there opens the Marketplace on its
**Extensions** tab, which only this patch adds: an extension with an Android
version comes first, with a switch, and any other says **Desktop only** and
opens its GitHub page. Without Theme colors, the **Themes** tab says what its
themes need.

For server files, enter an HTTPS WebDAV folder URL and credentials in
**Spicetify**, turn on **Use server files**, then select **Save and scan**.
After **Tracks ready** appears, return to **Settings and privacy > Apps and
devices**, enable **Local audio files**, then open **Local Files** in your
library. The server must support byte-range requests.

To update an existing Manager-signed installation, update **Spicetify Android
patches** in **Sources**, open Spotify's entry in Manager, and select **Patch**.
Install the resulting APK with the same Manager signing key to preserve app
data. If Android reports a certificate conflict, stop and check the signing
key before considering an uninstall.

## Return to stock Spotify

Uninstall the patched Spotify app, then reinstall the official Spotify app
from Google Play and sign in again. Uninstalling removes local app data and
downloaded music. Stock Spotify uses a different signing certificate, so it
cannot replace a Manager-signed installation through a normal app update.

## Try a local build

Build the bundle using the [development instructions](CONTRIBUTING.md), then
load `patches/build/libs/patches-*.mpp` in
[Morphe Desktop](https://github.com/MorpheApp/morphe-desktop). Select your own
stock Spotify APK or split-APK archive matching the declared target.

Keep the patcher's signing key if you use Desktop for later updates. A
Desktop-signed APK and a Manager-signed APK can use different keys.

## Development

Read [CONTRIBUTING.md](CONTRIBUTING.md) for setup, tests, and release steps.

This project is independent of Spotify and the Morphe project. Its repository
fork slug is `morphe-spicetify-patches`; its upstream project is `spicetify/morphe-patches`.

## License

The patch code is licensed under [GPL-3.0](LICENSE), with the upstream
[NOTICE](NOTICE) retained. See [third-party sources](THIRD_PARTY_NOTICES.md)
for the template and historical implementations used during development.
