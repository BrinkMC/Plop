package com.brinkmc.plop.shared.command.plot.nexus

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.util.cmd.CmdAddon
import com.brinkmc.plop.shared.util.message.MessageKey
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.incendo.cloud.annotations.Command

internal class CommandPlotVisitToggle(override val plugin: Plop) : Addon, CmdAddon {

    @Command("plot toggle visit")
    suspend fun toggleVisit(
        sender: CommandSourceStack
    ){
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

        if (!player.hasPermission("plop.plot.visit.toggle")) {
            player.sendMiniMessage(MessageKey.NO_PERMISSION)
            return
        }

        plot.visit.visitable = !plot.visit.visitable // Swap state
        player.sendMiniMessage(MessageKey.PLOT_TOGGLE_VISIT )

    }
}