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
    description = "RISKY experimental stream/preroll slot override; default OFF for playback safety. " +
        "The hotfix restores native slots when disabled; avoid enabling if songs auto-skip.",
    default = false,
) {
    compatibleWith(spotifyCompatibility)
    dependsOn(playerBridgePatch)

    execute {
        enableSetting("disableAudioAdBreaks")
    }
}
