package com.brinkmc.plop.shared.command.plot.claim

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.CommandDescription

internal class CommandPlotClaim(override val plugin: Plop): Addon {

    @Command("claim")
    @CommandDescription("claim a plot which is being previewed")
    suspend fun plotClaim(
        sender: Player
    ) {
        val potentialPreview = plots.previewHandler.getPreview(sender.uniqueId)


        if (potentialPreview == null) {
            sender.sendFormattedMessage(lang.get("command.no-preview", sender))
            return
        }

        plots.claimHandler.initiateClaim(sender.uniqueId, potentialPreview.type)
    }
}