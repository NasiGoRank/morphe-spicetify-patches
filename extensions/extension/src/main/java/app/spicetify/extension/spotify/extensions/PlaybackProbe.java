package app.spicetify.extension.spotify.extensions;

import android.util.Log;
import app.spicetify.extension.spotify.settings.InstalledPatches;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Opt-in, privacy-conscious diagnostic for Spotify Android playback constraints.
 *
 * The Connect device type is intentionally NOT spoofed: a value displayed by
 * Spotify Connect does not establish which server-side entitlement checks run.
 * Only booleans and RPC outcomes are retained. No track URIs, tokens, user IDs,
 * device IDs, or account information are collected.
 */
public final class PlaybackProbe {
    private static final String TAG = "SpicetifyProbe";
    private static final String PLAYER = Esperanto.CONTEXT_PLAYER;
    private static final long OBSERVATION_MS = 1500;
    private static final AtomicLong RUN = new AtomicLong();

    private static volatile Boolean shuffleNow;
    private static volatile boolean forcedShuffle;
    private static volatile boolean hasPlayerState;
    private static volatile boolean running;
    private static volatile String startingShuffle = "unknown";
    private static volatile String afterSetOptions = "not tested";
    private static volatile String afterDirect = "not tested";
    private static volatile String setOptionsResult = "not tested";
    private static volatile String directResult = "not tested";

    private static final PlayerBridge.StateListener LISTENER = state -> {
        if (state == null) return;
        hasPlayerState = true;
        shuffleNow = state.shuffleEnabled;
        forcedShuffle = state.forceShuffleSuppression;
    };

    private PlaybackProbe() {}

    public static void onCosmosConnected() {
        if (!InstalledPatches.playbackProbe()) return;
        hasPlayerState = false;
        shuffleNow = null;
        forcedShuffle = false;
        PlayerBridge.addStateListener(LISTENER);
    }

    /** Starts the two commands from the settings page, never on the Android UI thread. */
    public static void run() {
        if (!InstalledPatches.playbackProbe() || running) return;
        long id = RUN.incrementAndGet();
        startingShuffle = snapshotShuffle();
        afterSetOptions = "waiting";
        afterDirect = "waiting";
        setOptionsResult = "waiting";
        directResult = "waiting";
        running = true;

        PlayerBridge.post(() -> {
            if (!PlayerBridge.connected()) {
                setOptionsResult = "Player bridge not connected";
                directResult = "not tested";
                running = false;
                return;
            }
            PlayerBridge.call(PLAYER, "SetOptions", ShuffleOff.shuffleOffRequest(), new PlayerBridge.Result() {
                @Override
                public void done(byte[] body) {
                    setOptionsResult = parseRpcResponse(body);
                    observeThenTryDirect(id);
                }

                @Override
                public void failed(String reason) {
                    setOptionsResult = "transport error: " + safeError(reason);
                    observeThenTryDirect(id);
                }
            });
        });
    }

    private static void observeThenTryDirect(long id) {
        PlayerBridge.postDelayed(() -> {
            if (id != RUN.get()) return;
            afterSetOptions = snapshotShuffle();
            PlayerBridge.call(PLAYER, "SetShufflingContext", directShuffleOffRequest(), new PlayerBridge.Result() {
                @Override
                public void done(byte[] body) {
                    directResult = parseRpcResponse(body);
                    finishAfterObservation(id);
                }

                @Override
                public void failed(String reason) {
                    directResult = "transport error: " + safeError(reason);
                    finishAfterObservation(id);
                }
            });
        }, OBSERVATION_MS);
    }

    private static void finishAfterObservation(long id) {
        PlayerBridge.postDelayed(() -> {
            if (id != RUN.get()) return;
            afterDirect = snapshotShuffle();
            running = false;
            // Logs never include the playing track, an account identifier, or a device id.
            Log.i(TAG, report().replace('\n', ' '));
        }, OBSERVATION_MS);
    }

    /** SetShufflingContextRequest{1 shuffling_context = false}. */
    static byte[] directShuffleOffRequest() {
        Wire.Writer out = new Wire.Writer();
        out.bool(1, false);
        return out.toByteArray();
    }

    private static String parseRpcResponse(byte[] body) {
        if (body == null) return "HTTP 200, null body (not verified)";
        try {
            int error = Esperanto.parseResult(body);
            if (error == Esperanto.FORBIDDEN) return "HTTP 200; player reported FORBIDDEN (1)";
            if (error != Esperanto.OK) return "HTTP 200; player returned error " + error;
            // 200/code=0 still does not prove current playback has changed.
            return "HTTP 200; no error reported (check observed state)";
        } catch (IOException ex) {
            return "HTTP 200; response protobuf could not be parsed";
        }
    }

    /** Avoid embedding URLs or other untrusted error details in the diagnostic report. */
    private static String safeError(String reason) {
        if (reason == null) return "unknown";
        if (reason.startsWith("status ")) return reason.length() <= 20 ? reason : "HTTP error";
        if (PlayerBridge.NOT_CONNECTED.equals(reason)) return "disconnected";
        return "internal request failed (see logcat)";
    }

    private static String snapshotShuffle() {
        if (!hasPlayerState) return "unknown (no state received)";
        if (shuffleNow == null) return "unknown (options not reported)";
        return shuffleNow ? "ON" : "OFF";
    }

    /** Human-readable, redacted report, safe to copy from the settings page. */
    public static String report() {
        return "Android playback probe (Connect device type not modified)\n"
                + "Player bridge: " + (PlayerBridge.connected() ? "connected" : "not connected") + "\n"
                + "Live shuffle: " + snapshotShuffle() + "\n"
                + "mft/force_shuffle suppression: " + (hasPlayerState ? (forcedShuffle ? "present" : "not seen") : "unknown") + "\n"
                + "Shuffle before probe: " + startingShuffle + "\n"
                + "SetOptions: " + setOptionsResult + "\n"
                + "Shuffle after SetOptions: " + afterSetOptions + "\n"
                + "SetShufflingContext: " + directResult + "\n"
                + "Shuffle after direct RPC: " + afterDirect + "\n"
                + (running ? "Probe is running. Refresh shortly.\n" : "")
                + "NOTE: API success does not unlock Free playback or imply device identity changed.";
    }
}
