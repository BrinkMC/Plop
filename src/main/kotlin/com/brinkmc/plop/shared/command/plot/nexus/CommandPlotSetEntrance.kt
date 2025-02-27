package com.brinkmc.plop.shared.command.plot.nexus

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.util.cmd.CmdAddon
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.incendo.cloud.annotations.Command

internal class CommandPlotSetEntrance(override val plugin: Plop): Addon, CmdAddon {

    @Command("plot set entrance")
    suspend fun setEntrance(
        sender: CommandSourceStack
    ) {
        val player = getPlayer(sender.sender)
        val plot = player.getCurrentPlot()

        if (plot == null) {
            player.sendMiniMessage("plot.not-in-claim")
            return
        }

        if (!plot.owner.isPlayer(player)) {
            player.sendMiniMessage("plot.not-owner")
            return
        }

        if (!player.hasPermission("plop.plot.command.setentrance")) {
            player.sendMiniMessage("plot.no-permission")
            return
        }

        plot.claim.visit = player.location
        player.sendMiniMessage("plot.entrance-set")
    }
}