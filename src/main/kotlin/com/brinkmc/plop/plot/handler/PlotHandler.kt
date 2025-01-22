package com.brinkmc.plop.plot.handler

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.plot.plot.base.PlotOwner
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.storage.PlotKey
import com.brinkmc.plop.shared.util.sync
import org.bukkit.Location
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.filter

class PlotHandler(override val plugin: Plop): Addon, State  {

    private val plotMap: ConcurrentHashMap<PlotKey, Plot> = ConcurrentHashMap() // Ensure it is thread safe when accessed

    override suspend fun load() {

    }

    override suspend fun kill() {
        for (plot in plotMap) {
            plots.databasePlot.save(plot.value) // Save before refresh
        }
    }

    // Search functions
    fun getPlotById(plotId: UUID): Plot? {
        return plotMap[PlotKey(plotId = plotId)]
    }

    // Get plot by owner (for PersonalPlot)
    fun getPlotByOwner(ownerId: UUID): Plot? {
        return plotMap[PlotKey(ownerId = ownerId)]
    }

    // Get plot by owner or guild name (depending on the type)
    fun getPlotByGuildOwner(ownerId: UUID): Plot? {
        val potentialGuild = plugin.hooks.guilds.guildAPI.getGuild(ownerId)
        return plotMap[PlotKey(potentialGuild?.guildMaster?.uuid)]
    }

    fun getPlotByPlayer(playerId: UUID): List<Plot> {
        return plotMap.filter {
            it.value.plotId == playerId || ((it.value.owner) as PlotOwner.GuildOwner).members.contains(playerId)
        }.values.toList()
    }

    suspend fun addPlot(plot: Plot) = sync {
        val plotKey = PlotKey(plotId = plot.plotId, ownerId = plot.ownerId)
        plotMap[plotKey] = plot // Update cache synchronously
        plots.databasePlot.create(plot) // Run database update on IO
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