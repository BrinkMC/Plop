package com.brinkmc.plop.plot.handler

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.plot.plot.base.PlotOwner
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.plot.storage.PlotCache
import com.brinkmc.plop.plot.storage.PlotKey
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.hooks.Locals.world
import com.github.yannicklamprecht.worldborder.api.WorldBorderApi
import kotlinx.coroutines.delay
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.plugin.RegisteredServiceProvider
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import kotlin.collections.HashMap

class PlotHandler(override val plugin: Plop): Addon, State  {

    private lateinit var plotCache: PlotCache

    lateinit var borderAPI: WorldBorderApi // Late init as it is not available on startup

    override suspend fun load() { plugin.sync {
        plotCache = PlotCache(plugin) // Load the cache + database

        borderAPI = server.servicesManager.getRegistration<WorldBorderApi?>(WorldBorderApi::class.java)?.provider ?: run {
            logger.error("Failed to get WorldBorderAPI")
            return@sync
        }
    }}


    override suspend fun kill() {
        plotCache.kill()
    }

    // Search functions
    suspend fun getPlotById(plotId: UUID): Plot? {
        return plotCache.getPlotById(plotId)
    }

    // Get plot by owner (for PersonalPlot)
    suspend fun getPlotByOwner(ownerId: UUID?): Plot? {
        return plotCache.getPlotByOwner(ownerId)
    }

    suspend fun getPlotsByMembership(playerId: UUID): List<Plot> { // Gets all plots someone is in
        return listOfNotNull(
            getPlotByOwner(playerId),
            getPlotByOwner(playerId.guild()?.id)
        )
    }

    suspend fun hasGuildPlot(player: UUID): Boolean {
        return getPlotByOwner(player.guild()?.id) == null
    }

    suspend fun hasPersonalPlot(player: UUID): Boolean {
        return getPlotByOwner(player) == null
    }

    suspend fun addPlot(plot: Plot) {
        plotCache.addPlot(plot)
    }

    suspend fun getPlotFromLocation(location: Location): Plot? {
        val worldGuardRegions = plugin.hooks.worldGuard.getRegions(location) // WorldGuard is async so it is fine to access async
        if (worldGuardRegions?.size != 1) return null // Must only be one region player is standing in if it is a plot world
        return getPlotById(UUID.fromString(worldGuardRegions.first()?.id))
    }
    
    fun getPlotWorlds(): List<World> {
        return listOfNotNull(
            plotConfig.getPlotWorld(PlotType.PERSONAL)?.world(),
            plotConfig.getPlotWorld(PlotType.GUILD)?.world()
        )
    }

    suspend fun updateBorder(player: UUID) { plugin.sync {
            val bukkitPlayer = plugin.server.getPlayer(player) ?: run {
                logger.error("Failed to send fake border to player $player")
                return@sync
            }

            if (getPlotWorlds().contains(bukkitPlayer.world)) { // Run logic for getting plot they are in
                val plot = getPlotFromLocation(bukkitPlayer.location)

                if (plot == null) { // Make sure plot isn't null
                    logger.error("Unable to get the plot for player $player when updating border")
                    return@sync
                }

                borderAPI.setBorder(bukkitPlayer, plot.size.current.toDouble(), plot.claim.centre) // Send the player the border
            } else {
                borderAPI.resetWorldBorderToGlobal(bukkitPlayer);
            }
    } }
}