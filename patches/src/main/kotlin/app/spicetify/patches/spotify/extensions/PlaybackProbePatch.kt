package app.spicetify.patches.spotify.extensions

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.spicetify.patches.spotify.settings.enableSetting
import app.spicetify.patches.spotify.spotifyCompatibility
import com.android.tools.smali.dexlib2.iface.value.IntEncodedValue

/**
 * Read-only player state tracing plus opt-in manual RPC probes.
 *
 * A displayed Connect device type is NOT treated as a Premium entitlement.
 * The snapshot guard refuses unverified Spotify protobuf layouts. No
 * client registration, device identity, tokens, or account state are changed.
 */
@Suppress("unused")
val playbackProbePatch = bytecodePatch(
    name = "Playback identity and Shuffle probe (experimental)",
    description = "Records the live shuffle state and mft/force_shuffle signal, and compares " +
        "SetOptions versus SetShufflingContext using a manual button. Does not spoof " +
        "desktop identity or unlock Premium; never collects track or account identifiers.",
    default = false,
) {
    compatibleWith(spotifyCompatibility)
    dependsOn(playerBridgePatch)

    execute {
        for ((type, name, expected) in listOf(
            Triple(
                "Lcom/spotify/player/esperanto/proto/EsContextPlayerState\$ContextPlayerState;",
                "OPTIONS_FIELD_NUMBER",
                17
            ),
            Triple(
                "Lcom/spotify/player/esperanto/proto/EsContextPlayerState\$ContextPlayerState;",
                "SUPPRESSIONS_FIELD_NUMBER",
                19
            ),
            Triple(
                "Lcom/spotify/player/esperanto/proto/EsContextPlayerOptions\$ContextPlayerOptions;",
                "SHUFFLING_CONTEXT_FIELD_NUMBER",
                1
            ),
            Triple(
                "Lcom/spotify/player/esperanto/proto/EsSetOptions\$SetOptionsRequest;",
                "SHUFFLING_CONTEXT_FIELD_NUMBER",
                3
            )
        )) {
            val actual = (classDefByOrNull(type)?.staticFields?.firstOrNull { it.name == name }
                ?.initialValue as? IntEncodedValue)?.value
            if (actual != expected) {
                throw PatchException(
                    "Playback probe protocol changed: $type#$name expected $expected, got $actual. " +
                        "Requires verified Spotify 9.1.80.2221 ARM64 APKM."
                )
            }
        }

        enableSetting("playbackProbe")
    }
}
