package app.spicetify.extension.spotify.extensions;

import android.util.Log;
import app.spicetify.extension.spotify.settings.InstalledPatches;
import app.spicetify.extension.spotify.settings.PatchSettings;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Experimental audio-ad slot control through the existing Cosmos player bridge.
 *
 * Inspired by Pablobaro Morphe's DisableAdBreaksPatch (GPL-3.0) and desktop
 * Adblockify's ad-slot API (MIT). This is an independent implementation.
 * A successful request is NOT proof that the Spotify server will suppress ads.
 */
public final class AudioAdBreaks {
    private static final String SETTINGS_SERVICE = "spotify.ads.esperanto.proto.Settings";
    private static final String METHOD = "updateSlotEnabled";
    private static final String[] SLOTS = {"stream", "preroll"};
    private static final long FIRST_DELAY_MS = 10_000;
    private static final long REAPPLY_MS = 120_000;
    private static final AtomicLong GENERATION = new AtomicLong();

    private AudioAdBreaks() {}

    public static void onCosmosConnected() {
        reschedule(FIRST_DELAY_MS);
    }

    /** Called when the user changes the switch. A switch-off stops future calls. */
    public static void onPreferenceChanged() {
        reschedule(0);
    }

    /**
     * Safety behavior: cancelling the experimental ad suppression also asks Spotify
     * to re-enable its default ad slots. The old patch left those slots disabled.
     * The first launch of this hotfix resets the opt-in preference to OFF.
     */
    private static void reschedule(long delay) {
        long generation = GENERATION.incrementAndGet();
        if (!InstalledPatches.disableAudioAdBreaks()) return;
        if (PatchSettings.disableAudioAdBreaksEnabled()) {
            PlayerBridge.postDelayed(() -> apply(generation), delay);
        } else {
            // Restore immediately instead of waiting for a 120s reapply cycle.
            PlayerBridge.postDelayed(() -> restore(generation), Math.min(delay, 1000L));
        }
    }

    private static void restore(long generation) {
        if (generation != GENERATION.get() || !PlayerBridge.connected()) return;
        for (String slot : SLOTS) {
            sendSlotUpdate(slot, true);
        }
    }

    private static void apply(long generation) {
        if (generation != GENERATION.get()
                || !InstalledPatches.disableAudioAdBreaks()
                || !PatchSettings.disableAudioAdBreaksEnabled()) return;
        if (PlayerBridge.connected()) {
            for (String slot : SLOTS) sendSlotUpdate(slot, false);
        }
        PlayerBridge.postDelayed(() -> apply(generation), REAPPLY_MS);
    }

    /**
     * UpdateSlotEnabledRequest protobuf: 1 slot_id (string), 2 enabled (bool).
     * Write the explicit false boolean, including its tag, not merely an absent field.
     */
    static byte[] disableSlotRequest(String slot) {
        return slotEnabledRequest(slot, false);
    }

    /** Restore the native ad break slot that an earlier experimental build disabled. */
    static byte[] slotEnabledRequest(String slot, boolean enabled) {
        Wire.Writer request = new Wire.Writer();
        request.string(1, slot);
        request.bool(2, enabled);
        return request.toByteArray();
    }

    private static void sendSlotUpdate(String slot, boolean enabled) {
        PlayerBridge.call(SETTINGS_SERVICE, METHOD, slotEnabledRequest(slot, enabled), new PlayerBridge.Result() {
            @Override
            public void done(byte[] body) {
                // HTTP 200 only proves that the transport responded, not what the player accepted.
                Log.i("Spicetify", "Audio slot " + slot + " " + (enabled ? "restore" : "disable")
                        + " HTTP 200 (result not verified)");
            }
            @Override
            public void failed(String reason) {
                Log.w("Spicetify", "Audio slot " + slot + " update failed: " + reason);
            }
        });
    }
}
