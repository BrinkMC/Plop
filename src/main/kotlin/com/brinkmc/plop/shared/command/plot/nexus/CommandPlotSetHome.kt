package com.brinkmc.plop.shared.command.plot.nexus

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.constant.MessageKey
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.incendo.cloud.annotations.Command

internal class CommandPlotSetHome(override val plugin: Plop) : Addon {

    @Command("plot set home")
    suspend fun setHome(
        sender: CommandSourceStack
    ) {
        val player = getPlayer(sender.sender)
        val plot = player.getCurrentPlot()

        if (plot == null) {
            player.sendMiniMessage(MessageKey.NOT_PLOT)
            return
        }

        if (!plot.owner.isPlayer(player)) {
            player.sendMiniMessage(MessageKey.NOT_OWNER)
            return
        }

        if (!player.hasPermission("plop.plot.command.sethome")) {
            player.sendMiniMessage(MessageKey.NO_PERMISSION)
            return
        }

        plot.claim.home = player.location
        player.sendMiniMessage(MessageKey.PLOT_SET_HOME)
    }
}


