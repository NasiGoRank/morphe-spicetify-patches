# Experimental audio ad breaks (Spotify Android)

The new **Disable audio ad breaks (experimental)** patch uses the existing
`PlayerBridge` Cosmos hook to request disabled `stream` and `preroll`
audio ad slots. It periodically reapplies the request while enabled.

- Opt in to **Disable audio ad breaks (experimental)** in Morphe when patching
  the verified **Spotify 9.1.80.2221 ARM64/APKM** release.
- In Spotify, the toggle is under **Spicetify > Ads**.
- If the toggle is disabled, the patch stops future requests; restarting
  Spotify is required to restore the native state.
- It is **not** a verified full ad blocker, entitlement override, or
  guarantee of ad-free playback. Audio advertisements and Premium upsell
  prompts can still appear.
- Other existing independent patches on main include **Hide Home and Browse
  ads**, **Hide player ad cards**, and **Hide Premium tab**. Hiding the Premium
  navigation tab does **not** hide all Premium offer popups.
- `Request Shuffle OFF`, Desktop identity experiments and ordered playback
  probes were intentionally **not** merged into main. Testing on a Spotify
  Free Android device showed both shuffle toggling RPCs returning
  `FORBIDDEN (1)`. An ordered Play request returned OK but Shuffle remained ON.

### Validation

The unit test `AudioAdBreaksTest` checks the exact protobuf wire encoding
of both slot requests. Gradle builds test Java classes and the Morphe Android
bundle. Actual changes to Spotify's advertising are server-dependent and
are not verified by CI.
