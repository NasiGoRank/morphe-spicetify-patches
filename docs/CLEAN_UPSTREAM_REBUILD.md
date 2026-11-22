# Clean Spicetify upstream rebuild (diagnostic preview)

Source: **spicetify/morphe-patches**, upstream commit `6741682b28f5c120e7e995537b49705a5d8789ec` (October 19, 2026). This preview builds that source unchanged. Only release metadata and this diagnostic workflow/document differ.

Target Spotify: **9.1.80.2221**, Android ARM64, APKM, version code **145767611**.

Preview source URL:

```
https://raw.githubusercontent.com/NasiGoRank/morphe-spicetify-patches/refs/heads/rebuild/clean-spicetify-upstream/patches-bundle.json
```

This branch contains **no** local:
- Disable audio ad breaks experiment
- Premium promo popup blocker
- Forced Shuffle OFF experiments
- Ordered playback / Desktop identity probe

It **does** contain the upstream Spicetify patches, including *Spicetify extensions* and its optional Shuffle+ extension; do not select Spicetify extensions for the first test.

## Important limitations

The upstream code is the same original ABI-compatible baseline used by the earlier `abi-preview` build. A fresh compile may fix a damaged download, but **does not automatically fix a runtime crash** rooted in the same code. A passing CI, lint, or bytecode-patch build **does not** prove that Spotify launches or plays on a specific phone.

## Controlled recovery procedure

1. Keep a copy of any relevant local settings/files; uninstalling Spotify clears downloaded offline tracks and local app storage. Do not remove the Spotify account, playlists, or server-side library.
2. Test the **unmodified official Spotify client** with the same account and network. Confirm it can open, load playlists, play one song, and use Next.
3. If the official client works, acquire a clean Spotify APKM **9.1.80.2221** ARM64 (version code 145767611). Do not use an already patched APK, a different package variant, or a mismatched version.
4. Patch with this preview source and select only **Clean sharing links**. All other patches OFF, especially Spicetify extensions and ad/premium UI patches.
5. Do a **clean install**, not an update over a crashed patched app; this can remove downloaded tracks and local settings. Play one track from Search, then a playlist. Record the stage at which anything fails.

If even the official, unpatched app fails to play, stop modifying packages and investigate the network/account/Spotify service instead.

## ADB collection for startup crashes

On a PC with ADB, capture a focused log after launching the newly patched app:

```powershell
adb logcat -c
adb shell monkey -p com.spotify.music 1
adb logcat -d -v time -s AndroidRuntime:E
```

Share only the `FATAL EXCEPTION` and `Caused by` lines, after removing personal identifiers. If the app opens but playback endlessly skips, include a filtered `Spicetify` log and screenshot of the selected patches.

No APK is distributed by this repository. The preview releases only a patch bundle `.mpp`.
