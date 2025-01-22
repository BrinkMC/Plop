package com.brinkmc.plop.plot.handler

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.plot.plot.base.PlotOwner
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.hooks.Locals.world
import com.brinkmc.plop.shared.storage.PlotKey
import com.brinkmc.plop.shared.util.async
import com.brinkmc.plop.shared.util.sync
import com.github.yannicklamprecht.worldborder.api.WorldBorderApi
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.plugin.RegisteredServiceProvider
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class PlotHandler(override val plugin: Plop): Addon, State  {

    private val plotMap: ConcurrentHashMap<PlotKey, Plot> = ConcurrentHashMap() // Ensure it is thread safe when accessed
    private val borderAPI: WorldBorderApi? = server.servicesManager.getRegistration<WorldBorderApi?>(WorldBorderApi::class.java)?.provider

    override suspend fun load() {}

    override suspend fun kill() = sync {
        for (plot in plotMap.toMap()) {
            plots.databaseHandler.save(plot.value) // Save before refresh
        }
    }

    fun getPlotMap(): Map<PlotKey, Plot> {
        return Collections.unmodifiableMap(plotMap)
    }

    // Search functions
    fun getPlotById(plotId: UUID): Plot? {
        return plotMap[PlotKey(plotId = plotId)]
    }

    // Get plot by owner (for PersonalPlot)
    fun getPlotByOwner(ownerId: UUID?): Plot? {
        return plotMap[PlotKey(ownerId = ownerId)]
    }

    fun getPlotsByMembership(playerId: UUID): List<Plot> { // Not efficient DO NOT UISE
        return plotMap.filter {
            it.value.plotId == playerId || ((it.value.owner) as PlotOwner.GuildOwner).members.contains(playerId)
        }.values.toList()
    }

    fun hasGuildPlot(player: UUID): Boolean {
        return getPlotByOwner(plugin.hooks.guilds.guildAPI.getGuild(player)?.id) == null
    }

    fun hasPersonalPlot(player: UUID): Boolean {
        return getPlotByOwner(player) == null
    }

    suspend fun addPlot(plot: Plot) = sync {
        val plotKey = PlotKey(plotId = plot.plotId, ownerId = plot.ownerId)
        plotMap.putIfAbsent(plotKey, plot)  // Update cache synchronously
        plots.databaseHandler.create(plot) // Run database update on IO
    }

    suspend fun getPlotFromLocation(location: Location): Plot? = async {
        val worldGuardRegions = plugin.hooks.worldGuard.getRegions(location) // WorldGuard is async so it is fine to access async
        if (worldGuardRegions?.size != 1) return@async null // Must only be one region player is standing in if it is a plot world
        return@async getPlotById(UUID.fromString(worldGuardRegions.first()?.id))
    }
    
    fun getPlotWorlds(): List<World> {
        return listOfNotNull(
            plotConfig.getPlotWorld(PlotType.PERSONAL).world(),
            plotConfig.getPlotWorld(PlotType.GUILD).world()
        )
    }

    suspend fun updateBorder(player: UUID) = async {
        val bukkitPlayer = plugin.server.getPlayer(player) ?: run {
            logger.error("Failed to send fake border to player $player")
            return@async
        }

        if (getPlotWorlds().contains(bukkitPlayer.world)) { // Run logic for getting plot they are in
            val plot = getPlotFromLocation(bukkitPlayer.location)

            if (plot == null) { // Make sure plot isn't null
                logger.error("Unable to get the plot for player $player when updating border")
                return@async
            }

            borderAPI?.setBorder(bukkitPlayer, plot.size.current.toDouble(), plot.claim.centre) // Send the player the border
        } else {
            borderAPI?.resetWorldBorderToGlobal(bukkitPlayer);
        }
    }
}