package app.spicetify.extension.spotify.extensions;

import android.util.Log;
import app.spicetify.extension.spotify.settings.InstalledPatches;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manual, opt-in test of a DIFFERENT playback route: request the complete
 * playlist/album in its original order in one ContextPlayer/Play request.
 *
 * Unlike SetOptions or SetShufflingContext, this uses the already existing
 * PreparePlayRequest with an ordered ContextPage and a shuffle=false override.
 * The server may still reject this for a Free account. The current playback
 * restarts from track one ONLY when the user explicitly taps the test button.
 */
public final class OrderedPlayback {
    private static final String TAG = "SpicetifyOrderProbe";
    private static final int PAGE = 300;
    private static final int MAX_TRACKS = 1500;
    private static final long RETRY_LOADING_MS = 1200L;
    private static final int MAX_LOADING_RETRIES = 3;

    private static final AtomicBoolean RUNNING = new AtomicBoolean();
    private static volatile String status = "Not tested";
    private static volatile String type = "none";
    private static volatile int tracks;
    private static volatile String shuffleBefore = "unknown";
    private static volatile String shuffleAfter = "not observed";

    private OrderedPlayback() {}

    /** Trigger ONLY from the dedicated Spicetify diagnostic page action. */
    public static void run() {
        if (!InstalledPatches.playbackProbe()) {
            status = "Playback probe patch is not installed";
            return;
        }
        if (!RUNNING.compareAndSet(false, true)) {
            status = "A previous ordered-playback test is still running";
            return;
        }
        type = "unknown";
        tracks = 0;
        shuffleBefore = "unknown";
        shuffleAfter = "not observed";
        status = "Reading current playlist; no playback changes yet";
        PlayerBridge.post(() -> {
            if (!PlayerBridge.connected()) {
                done("Player bridge not connected");
                return;
            }
            Esperanto.PlayerState state = PlayerBridge.lastState();
            if (state == null || state.contextUri == null) {
                done("No live playlist context available; start a playlist first");
                return;
            }

            shuffleBefore = shuffleLabel(state);
            String context = state.contextUri;
            if (Esperanto.isLikedSongs(context)) context = Esperanto.LIKED_SONGS;
            if (!supportedContext(context)) {
                done("Only playlists, albums and Liked Songs are supported");
                return;
            }
            type = context.startsWith("spotify:album:") ? "album" : "playlist";
            new Run(context).start();
        });
    }

    static boolean supportedContext(String context) {
        return context != null && (context.startsWith("spotify:playlist:")
                || context.startsWith("spotify:album:"));
    }

    private static String shuffleLabel(Esperanto.PlayerState state) {
        if (state == null || state.shuffleEnabled == null) return "unknown";
        return state.shuffleEnabled ? "ON" : "OFF";
    }

    private static void done(String message) {
        status = message;
        RUNNING.set(false);
        Log.i(TAG, "Ordered playback result: " + message);
    }

    private static String safeReason(String reason) {
        if (PlayerBridge.NOT_CONNECTED.equals(reason)) return "Player bridge disconnected";
        return "Transport failed (check logcat for diagnostic context)";
    }

    /** User-facing report intentionally excludes track URIs, playlist titles, and account IDs. */
    public static String report() {
        return "Ordered playback test (Desktop identity not spoofed)\n"
                + "Status: " + status + "\n"
                + "Context type: " + type + "\n"
                + "Tracks listed: " + tracks + "\n"
                + "Shuffle before: " + shuffleBefore + "\n"
                + "Shuffle after: " + shuffleAfter + "\n"
                + (RUNNING.get() ? "Test running; refresh results shortly.\n" : "")
                + "A successful Play response does not prove that Free shuffle restrictions disappeared.\n"
                + "WARNING: If Spotify accepts Play, this starts the playlist from its first song.";
    }

    private static final class Run {
        private final String context;
        private final List<String> ordered = new ArrayList<>();
        private int retries;

        Run(String context) {
            this.context = context;
        }

        void start() {
            if (context.startsWith("spotify:album:")) {
                call(Esperanto.METADATA, "GetEntity", Esperanto.getEntity(context), body -> {
                    ordered.addAll(Esperanto.parseAlbumTracks(body));
                    if (ordered.size() > MAX_TRACKS) {
                        done("Album exceeds " + MAX_TRACKS + "-track safety limit; playback unchanged");
                    } else {
                        play();
                    }
                });
            } else {
                listNext();
            }
        }

        private void listNext() {
            call(Esperanto.PLAYLIST, "Get", Esperanto.playlistGet(context, ordered.size(), PAGE), body -> {
                Esperanto.PlaylistPage page = Esperanto.parsePlaylistGet(body);
                if (page.loading) {
                    if (retries++ >= MAX_LOADING_RETRIES) {
                        done("Playlist is still loading; playback unchanged");
                    } else {
                        PlayerBridge.postDelayed(this::listNext, RETRY_LOADING_MS);
                    }
                    return;
                }
                if (page.length > MAX_TRACKS) {
                    done("Playlist exceeds " + MAX_TRACKS + "-track safety limit; playback unchanged");
                    return;
                }
                ordered.addAll(page.uris);
                tracks = ordered.size();
                if (ordered.size() > MAX_TRACKS) {
                    done("Playlist is too large; playback unchanged");
                } else if (page.uris.isEmpty() || ordered.size() >= page.length) {
                    play();
                } else {
                    listNext();
                }
            });
        }

        private void play() {
            tracks = ordered.size();
            if (ordered.isEmpty()) {
                done("No playable tracks returned; playback unchanged");
                return;
            }
            status = "Sending explicit original-order Play request";
            call(Esperanto.CONTEXT_PLAYER, "Play", Esperanto.playOrder(context, ordered), body -> {
                int code = Esperanto.parseResult(body);
                if (code == Esperanto.FORBIDDEN) {
                    done("Play rejected: FORBIDDEN (1); original order cannot be applied via this route");
                } else if (code != Esperanto.OK) {
                    done("Play rejected: player code " + code);
                } else {
                    status = "Play reported OK; observing player state";
                    PlayerBridge.postDelayed(() -> {
                        shuffleAfter = shuffleLabel(PlayerBridge.lastState());
                        done("Play reported OK; verify actual song sequence. Shuffle state may remain ON.");
                    }, 2200L);
                }
            });
        }

        private interface Step {
            void done(byte[] response) throws IOException;
        }

        private void call(String service, String method, byte[] request, Step step) {
            PlayerBridge.call(service, method, request, new PlayerBridge.Result() {
                @Override
                public void done(byte[] body) {
                    try {
                        step.done(body);
                    } catch (Throwable e) {
                        Log.w(TAG, "Ordered playback: response could not be processed", e);
                        OrderedPlayback.done("Could not parse Spotify response; playback outcome unknown");
                    }
                }

                @Override
                public void failed(String reason) {
                    OrderedPlayback.done(safeReason(reason));
                }
            });
        }
    }
}
