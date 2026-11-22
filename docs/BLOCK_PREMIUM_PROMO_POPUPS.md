# Block Premium promo popups (experimental)

Target: Spotify Android **9.1.80.2221**, ARM64/APKM, Morphe. Preview version **1.0.1-alpha.1**.

## Scope

The patch hooks the Pendragon FetchMessage and FetchMessageList RxJava requests, replacing a fullscreen marketing message fetch with the app's existing error fallback. It is based on the Pendragon hook technique in [pablobaro/pablobaro-morphe-patches](https://github.com/pablobaro/pablobaro-morphe-patches) (GPL-3.0), with a defensive fallback evaluation. If the fallback is unavailable, the original request is left unchanged.

It is **not** a Premium subscription bypass or a general dialog suppressor. A paywall shown when tapping a locked feature (for example, turning Shuffle OFF on Spotify Free) might use a separate flow and may still appear. It also does **not** remove all Home ads or interrupting audio ads.

The code is opt-in, defaults to OFF in Morphe, and **does not change main**.

## Test on Android

1. Add Morphe source URL:
   `https://raw.githubusercontent.com/NasiGoRank/morphe-spicetify-patches/refs/heads/experiment/hide-premium-promo-popups/patches-bundle.json`
2. Select **Block Premium promo popups (experimental)** for Spotify **9.1.80.2221 ARM64/APKM**. Existing audio ad patches can remain selected; to isolate the experiment, first try the new popup filter without other visual ad filters.
3. Open Spotify, **Spicetify → Ads**, and enable **Block Premium promo popups (experimental)**.
4. Force-close Spotify and reopen it a few times to trigger any startup promotions naturally. Do not click a locked feature just to test this path: that invokes a separate paywall.
5. In **Spicetify → Ads**, tap **Promo blocker status**. The integer counts substituted Pendragon fetches, not visual popups.
6. If you still get a fullscreen *Premium marketing popup*, record where it appeared (startup vs after a user tap), and provide a screenshot with personal details redacted. This indicates whether a second hook is necessary.

The runtime only replaces recognized RxJava SingleOnErrorReturn objects, and fails open on unknown response wrappers. The patch does not claim proven effectiveness before physical-device testing.

## Interpretation

- A status count greater than zero confirms that at least one promotional message fetch returned the built-in no-message fallback.
- Zero may mean no promotional message was requested, the fingerprint's execution path was not hit, or the callback was not recognized.
- If startup promotions disappear, confirm over repeated sessions, because promotions are episodic.
- Premium feature-gate dialogs may persist and require a distinct, narrowly scoped UI hook.

Patch experiments live on this separate branch. Once validated on the Android device, the stable code can be promoted to main by a dedicated PR.
