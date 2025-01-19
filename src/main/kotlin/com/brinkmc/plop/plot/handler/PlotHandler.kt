package com.brinkmc.plop.plot.handler

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.storage.PlotKey
import org.bukkit.Location
import java.util.UUID
import kotlin.collections.filter

class PlotHandler(override val plugin: Plop): Addon {

    private val plotMap = plots.plotMap

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
    fun getPlotsByOwnerOrGuild(ownerId: UUID): List<Plot> {
        val potentialGuild = plugin.hooks.guilds.guildAPI.getGuild(ownerId)
        return plotMap.filter { (key, _) ->
            (key.ownerId == ownerId || key.ownerId == )
        }.map { it.value }
    }

    fun addPlot(plot: Plot) {
        val plotKey = PlotKey(plotId = plot.plotId, ownerId = plot.ownerId)
        plotMap[plotKey] = plot
    }

    fun updateBorder(player: UUID) {
        val bukkitPlayer = plugin.server.getPlayer(player) ?: run {
            logger.error("Failed to send fake border to player $player")
            return
        }

        // Player outside the plot?
        if (bukkitPlayer.world.name !in listOf<String>(plotConfig.personalPlotWorld,
                plotConfig.guildPlotWorld) || player.getLocation().distanceSquared(center) > (maxLength * maxLength)) {
            BorderAPI.getApi().resetWorldBorderToGlobal(player);
        } else {
            BorderAPI.getApi().setBorder(player, getWorldBorderSize(), getCenter());
        }

    }

    fun getPlotFromLocation(location: Location): Plot? {
        return
    }
}