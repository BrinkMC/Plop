package com.brinkmc.plop.shared.command.plot.claim

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.command.utils.CmdAddon
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.command.CommandSender
import org.incendo.cloud.paper.util.sender.Source
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.CommandDescription
import org.incendo.cloud.paper.util.sender.PlayerSource

internal class CommandPlotClaim(override val plugin: Plop): Addon, CmdAddon {

    @Command("plot claim")
    @CommandDescription("claim a plot which is being previewed")
    suspend fun plotClaim(
        sender: CommandSourceStack
    ) {
        val player = getPlayer(sender.sender)

        val potentialPreview = plots.previewHandler.getPreview(player.uniqueId)

        if (potentialPreview == null) {
            player.sendFormattedMessage(lang.get("command.no-preview", player))
            return
        }

        if (player.world.name != potentialPreview.viewPlot.value.world) {
            player.sendFormattedMessage(lang.get("command.wrong-world", player))
            return
        }

        plots.claimHandler.initiateClaim(player.uniqueId, potentialPreview.type)
    }
}