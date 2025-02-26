package com.brinkmc.plop.shared.command.plot.general

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.util.cmd.CmdAddon
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.incendo.cloud.annotations.Command

class CommandNexusBook(override val plugin: Plop) : Addon, CmdAddon {

    @Command("nexus")
    suspend fun nexus(
        sender: CommandSourceStack
    ) {
        val player = getPlayer(sender.sender)

        if (player.inventory.contains(plots.nexusManager.NEXUS_BOOK)) {
            player.sendMiniMessage("plot.nexus.already-have")
            return
        }

        if (!player.world.isPlotWorld()) {
            player.sendMiniMessage("plot.nexus.not-in-plot-world")
            return
        }

        if (!player.getPlots().contains(player.getCurrentPlot())) {
            player.sendMiniMessage("plot.nexus.not-in-plot")
            return
        }

        player.inventory.addItem(plots.nexusManager.NEXUS_BOOK)
    }
}