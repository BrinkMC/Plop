package com.brinkmc.plop.shared.command.plot.nexus

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.util.CmdAddon
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
            player.sendMiniMessage(lang.get("plot.not-in-plot"))
            return
        }

        if (!plot.owner.getPlayers().contains(player.uniqueId)) {
            player.sendMiniMessage(lang.get("plot.not-owner"))
            return
        }

        if (!player.hasPermission("plop.plot.visit.toggle")) {
            player.sendMiniMessage(lang.get("plot.no-permission"))
            return
        }

        plot.visit.visitable = !plot.visit.visitable // Swap state
        player.sendMiniMessage(lang.get("plot.visit.toggle"))

    }
}