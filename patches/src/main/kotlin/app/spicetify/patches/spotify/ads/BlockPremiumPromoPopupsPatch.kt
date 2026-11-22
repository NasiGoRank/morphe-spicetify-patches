package app.spicetify.patches.spotify.ads

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstruction
import app.spicetify.patches.spotify.settings.enableSetting
import app.spicetify.patches.spotify.settings.settingsPatch
import app.spicetify.patches.spotify.spotifyCompatibility
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

/**
 * Target Pendragon message fetch RPCs, not arbitrary dialogs.
 * Based on GPL-3.0 pablobaro/pablobaro-morphe-patches BlockPopupAdsPatch.
 * The popup from clicking the locked Shuffle button is a separate paywall.
 */
@Suppress("unused")
val blockPremiumPromoPopupsPatch = bytecodePatch(
    name = "Block Premium promo popups (experimental)",
    description = "Attempts to hide fullscreen Pendragon promos using Spotify's own no-message " +
        "fallback. Does not hide Premium purchase sheets triggered by locked features.",
    default = false,
) {
    compatibleWith(spotifyCompatibility)
    dependsOn(settingsPatch)

    execute {
        enableSetting("blockPremiumPromoPopups")
    }

    finalize {
        // Run after Spicetify snapshot-verified class patches.
        val finders = listOf(
            Fingerprint(
                name = "apply",
                filters = listOf(methodCall(definingClass = "FetchMessageRequest;", name = "<init>")),
            ),
            Fingerprint(
                name = "apply",
                filters = listOf(methodCall(definingClass = "FetchMessageListRequest;", name = "<init>")),
            ),
        )
        val matches = finders.flatMap { it.matchAllOrNull().orEmpty() }
            .filter { it.method.returnType.startsWith("L") }
        if (matches.isEmpty()) throw PatchException(
            "Spotify Pendragon request ABI not found: do not patch unrelated popup methods."
        )

        val visited = mutableSetOf<String>()
        for (match in matches) {
            val method = match.method
            val methodKey = method.definingClass + "->" + method.name + method.parameterTypes.toString()
            if (!visited.add(methodKey)) continue
            val returns = method.instructions.withIndex()
                .filter { it.value.opcode == Opcode.RETURN_OBJECT }
            if (returns.isEmpty()) throw PatchException("Pendragon method has no RETURN_OBJECT")
            for ((index, original) in returns.asReversed()) {
                val register = (original as OneRegisterInstruction).registerA
                // Duplicate the original branch-target return, insert hook before
                // the duplicate, and remove original to keep branch labels.
                method.addInstruction(index + 1, method.getInstruction(index))
                method.addInstructions(index + 1, """
                    invoke-static/range {v$register .. v$register}, Lapp/spicetify/extension/spotify/ads/BlockPromoPopups;->replaceRequest(Ljava/lang/Object;)Ljava/lang/Object;
                    move-result-object v$register
                    check-cast v$register, """ + method.returnType)
                method.removeInstruction(index)
            }
        }
    }
}
