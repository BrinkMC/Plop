package com.brinkmc.plop.shared.command.admin

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.Command
import com.brinkmc.plop.shared.constant.MessageKey
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.incendo.cloud.annotations.Command

internal class CommandAdminUnclaimPlot(override val plugin: Plop) : Addon, Command {

    @Command("plop plot unclaim")
    suspend fun unclaimPlot(
        sender: CommandSourceStack
    ) {
        val player = getPlayer(sender.sender)

        if (!player.hasPermission("plop.admin.claim")) {
            player.sendMiniMessage(MessageKey.NO_PERMISSION)
            return
        }

        // Initiate unclaim based on player's location
        val plot = player.getCurrentPlot()

        if (plot == null) {
            player.sendMiniMessage(MessageKey.NO_PLOT)
            return
        }

        plugin.hooks.worldGuard.deleteRegion(plot.plotId)
        plots.handler.deletePlot(plot)
        player.sendMiniMessage(MessageKey.DELETE_SUCCESS)
    }

}


