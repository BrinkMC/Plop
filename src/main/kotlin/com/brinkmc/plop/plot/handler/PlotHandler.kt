package com.brinkmc.plop.plot.handler

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.GuildPlot
import com.brinkmc.plop.plot.plot.PersonalPlot
import com.brinkmc.plop.plot.plot.Plot
import com.brinkmc.plop.plot.plot.PlotKey
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import org.bukkit.entity.Player
import java.util.UUID

class PlotHandler(override val plugin: Plop): Addon {

    val plotMap = plots.plotMap

    // Search functions
    fun getPlotById(plotId: UUID): Plot? {
        return plotMap[PlotKey(plotId = plotId)]
    }

    // Get plot by owner (for PersonalPlot)
    fun getPlotByOwner(ownerId: UUID): Plot? {
        return plotMap[PlotKey(ownerId = ownerId)]
    }

    // Get plot by guild name (for GuildPlot)
    fun getPlotByGuild(guildId: UUID): Plot? {
        return plotMap[PlotKey(guildId = guildId)]
    }

    // Get plot by owner or guild name (depending on the type)
    fun getPlotsByOwnerOrGuild(ownerId: UUID?, guildId: UUID?): List<Plot> {
        return plotMap.filter { (key, _) ->
            (key.ownerId == ownerId || key.guildId == guildId)
        }.map { it.value }
    }

    fun addPlot(plot: Plot) {
        val plotKey = when (plot) {
            is GuildPlot -> PlotKey(plotId = plot.plotId, guildId = plot.guildId)
            is PersonalPlot -> PlotKey(plotId = plot.plotId, ownerId = plot.ownerId)
        }
        plotMap[plotKey] = plot
    }

}