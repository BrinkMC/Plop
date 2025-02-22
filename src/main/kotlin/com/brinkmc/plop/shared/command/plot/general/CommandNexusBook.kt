package com.brinkmc.plop.shared.command.plot.general

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.util.CmdAddon
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command

class CommandNexusBook(override val plugin: Plop) : Addon, CmdAddon {

    @Command("nexus")
    suspend fun nexus(
        sender: CommandSourceStack
    ) {
        val player = getPlayer(sender.sender)

        if (player.inventory.contains(plots.nexusManager.NEXUS_BOOK)) {
            player.sendMiniMessage(lang.get("plot.nexus.already-have"))
            return
        }

        if (!player.world.isPlotWorld()) {
            player.sendMiniMessage(lang.get("plot.nexus.not-in-plot-world"))
            return
        }

        if (!player.getPlots().contains(player.getCurrentPlot())) {
            player.sendMiniMessage(lang.get("plot.nexus.not-in-plot"))
            return
        }

        player.inventory.addItem(plots.nexusManager.NEXUS_BOOK)
    }
}