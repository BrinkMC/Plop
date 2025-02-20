package com.brinkmc.plop.shared.command.plot.nexus

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import org.incendo.cloud.paper.util.sender.Source
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.processing.CommandContainer
import org.incendo.cloud.paper.util.sender.PlayerSource

internal class CommandPlotSetHome(override val plugin: Plop) : Addon {

    @Command("plot sethome")
    suspend fun setHome(
        sender: PlayerSource
    ) {
        val player = sender as Player
        if (!player.inPlot()) {
            player.sendFormattedMessage(lang.get("plot.sethome.bad"))
            return
        }
    }

}


