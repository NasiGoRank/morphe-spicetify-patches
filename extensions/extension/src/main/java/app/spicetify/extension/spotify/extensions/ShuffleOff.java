package app.spicetify.extension.spotify.extensions;

import android.util.Log;
import app.spicetify.extension.spotify.settings.InstalledPatches;
import app.spicetify.extension.spotify.settings.PatchSettings;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Requests shuffle OFF from Spotify's player. This does not override the
 * entitlement checks on Free accounts, and the server may refuse or revert it.
 */
public final class ShuffleOff {
    private static final long FIRST_DELAY_MS = 2_500;
    private static final long CONTEXT_DELAY_MS = 1_500;
    private static final AtomicLong GENERATION = new AtomicLong();
    private static volatile String lastContext;
    private static volatile String lastResult = "No request sent yet";

    private static final PlayerBridge.StateListener CONTEXT_LISTENER = state -> {
        if (state == null || state.contextUri == null || state.contextUri.isEmpty()
                || !PatchSettings.forceShuffleOffEnabled()) return;
        String uri = state.contextUri;
        if (uri.equals(lastContext)) return;
        lastContext = uri;
        long generation = GENERATION.get();
        PlayerBridge.postDelayed(() -> requestOff(generation), CONTEXT_DELAY_MS);
    };

    private ShuffleOff() {}

    /** Called on every native router replacement. */
    public static void onCosmosConnected() {
        reschedule();
    }

    public static void onPreferenceChanged() {
        reschedule();
    }

    private static void reschedule() {
        long generation = GENERATION.incrementAndGet();
        lastContext = null;
        PlayerBridge.removeStateListener(CONTEXT_LISTENER);
        if (!InstalledPatches.forceShuffleOff() || !PatchSettings.forceShuffleOffEnabled()) return;
        PlayerBridge.addStateListener(CONTEXT_LISTENER);
        PlayerBridge.postDelayed(() -> requestOff(generation), FIRST_DELAY_MS);
    }

    private static void requestOff(long generation) {
        if (generation != GENERATION.get() || !InstalledPatches.forceShuffleOff()
                || !PatchSettings.forceShuffleOffEnabled() || !PlayerBridge.connected()) return;

        PlayerBridge.call(Esperanto.CONTEXT_PLAYER, "SetOptions", shuffleOffRequest(), new PlayerBridge.Result() {
            @Override
            public void done(byte[] body) {
                try {
                    int error = Esperanto.parseResult(body);
                    if (error == Esperanto.OK) {
                        // Accepted at the API level does not prove the server will keep it off.
                        lastResult = "Shuffle OFF request accepted; confirm actual playback order";
                        Log.i("Spicetify", lastResult);
                    } else {
                        lastResult = error == Esperanto.FORBIDDEN
                                ? "Spotify refused Shuffle OFF; Free plan restrictions may apply"
                                : "Spotify returned shuffle error " + error;
                        Log.w("Spicetify", lastResult);
                    }
                } catch (IOException e) {
                    lastResult = "Shuffle OFF response could not be read";
                    Log.w("Spicetify", lastResult, e);
                }
            }

            @Override
            public void failed(String reason) {
                lastResult = "Shuffle OFF request failed: " + reason;
                Log.w("Spicetify", lastResult);
            }
        });
    }

    /** Status shown in settings; no claim of successful playback without a device test. */
    public static String lastResult() {
        return lastResult;
    }

    /**
     * SetOptionsRequest field 3: OptionalBoolean shuffling_context, field 1 false.
     * This is the explicit OFF command, not a forged Premium account state.
     */
    static byte[] shuffleOffRequest() {
        Wire.Writer value = new Wire.Writer();
        value.bool(1, false);
        Wire.Writer request = new Wire.Writer();
        request.message(3, value);
        return request.toByteArray();
    }
}
