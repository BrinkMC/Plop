package com.brinkmc.plop.shared.command.plot.nexus

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.processing.CommandContainer

internal class CommandPlotSetHome(override val plugin: Plop) : Addon {

    @Command("plot sethome")
    suspend fun setHome(
        player: Player
    ) {
        if player.inPlot() {
            player.sendFormattedMessage(lang.get("plot-sethome-already"))
            return
        }
    }

}


