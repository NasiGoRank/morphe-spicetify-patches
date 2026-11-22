package app.spicetify.patches.spotify.extensions

import app.morphe.patcher.patch.bytecodePatch
import app.spicetify.patches.spotify.settings.enableSetting
import app.spicetify.patches.spotify.spotifyCompatibility

/**
 * Reuses the verified player bridge. The runtime sends ContextPlayer.SetOptions
 * with shuffling_context=false and reports errors, rather than spoofing Premium.
 */
@Suppress("unused")
val requestShuffleOffPatch = bytecodePatch(
    name = "Request Shuffle OFF (experimental)",
    description = "Asks Spotify to turn shuffle off on startup and context changes. " +
        "Free accounts may be refused by Spotify; does not unlock Premium.",
    default = false,
) {
    compatibleWith(spotifyCompatibility)
    dependsOn(playerBridgePatch)

    execute {
        enableSetting("forceShuffleOff")
    }
}
