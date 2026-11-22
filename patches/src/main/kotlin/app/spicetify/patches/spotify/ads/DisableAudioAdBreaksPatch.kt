package app.spicetify.patches.spotify.ads

import app.morphe.patcher.patch.bytecodePatch
import app.spicetify.patches.spotify.extensions.playerBridgePatch
import app.spicetify.patches.spotify.settings.enableSetting
import app.spicetify.patches.spotify.spotifyCompatibility

/**
 * Reuses the existing verified SharedCosmosRouterService hook. No extra native
 * fingerprint or unverified DEX injection point is needed.
 */
@Suppress("unused")
val disableAudioAdBreaksPatch = bytecodePatch(
    name = "Disable audio ad breaks (experimental)",
    description = "Requests disabling stream and preroll audio ad slots. Spotify may refuse or ignore the request; " +
        "does not unlock Premium or guarantee ad-free playback.",
    default = false,
) {
    compatibleWith(spotifyCompatibility)
    dependsOn(playerBridgePatch)

    execute {
        enableSetting("disableAudioAdBreaks")
    }
}
