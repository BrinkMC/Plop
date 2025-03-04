package com.brinkmc.plop.plot.handler

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.plot.plot.base.PlotOwner
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.plot.storage.PlotCache
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

    private lateinit var borderAPI: WorldBorderApi // Late init as it is not available on startup

    override suspend fun load() { syncScope {
        plotCache = PlotCache(plugin) // Load the cache + database
        plotCache.load()

        borderAPI = server.servicesManager.getRegistration<WorldBorderApi?>(WorldBorderApi::class.java)?.provider ?: run {
            logger.error("Failed to get WorldBorderAPI")
            return@syncScope
        }
    }}


    override suspend fun kill() {
        plotCache.kill()
    }

    // Search functions
    suspend fun getPlotById(plotId: UUID?): Plot? {
        if (plotId == null) return null
        return plotCache.getPlot(plotId)
    }

    suspend fun getPlotsByMembership(playerId: UUID): List<Plot> { // Gets all plots someone is in
        return listOfNotNull(
            getPlotById(playerId),
            getPlotById(playerId.guild()?.id)
        )
    }

    suspend fun hasGuildPlot(player: UUID): Boolean {
        return getPlotById(player.guild()?.id) != null
    }

    suspend fun hasPersonalPlot(player: UUID): Boolean {
        return getPlotById(player) != null
    }

    suspend fun addPlot(plot: Plot) {
        plotCache.addPlot(plot)
    }

    suspend fun deletePlot(plot: Plot) {
        plotCache.deletePlot(plot)
    }

    suspend fun getPlotFromLocation(location: Location): Plot? { return asyncScope {
        val worldGuardRegions = plugin.hooks.worldGuard.getRegions(location) // WorldGuard is async so it is fine to access async
        if (worldGuardRegions?.size != 1) return@asyncScope null // Must only be one region player is standing in if it is a plot world
        return@asyncScope getPlotById(UUID.fromString(worldGuardRegions.first()?.id))
    } }

    fun updateBorder(player: UUID) { plugin.async {
        val bukkitPlayer = plugin.server.getPlayer(player) ?: run {
            logger.error("Failed to send fake border to player $player")
            return@async
        }

        val potentialPreview = plots.previewHandler.getPreview(player)
        if (potentialPreview != null) { // Check if player is in a preview
            syncScope {
                borderAPI.setBorder(bukkitPlayer, plotConfig.getPlotMaxSize(potentialPreview.type).toDouble(), potentialPreview.viewPlot.value.toLocation())
            }
            return@async
        }


        if (bukkitPlayer.world.isPlotWorld()) { // Run logic for getting plot they are in
            val plot = getPlotFromLocation(bukkitPlayer.location) ?:  return@async // Make sure plot isn't null

            syncScope {
                borderAPI.setBorder(bukkitPlayer, plot.size.current.toDouble(), plot.claim.centre) // Send the player the border
            }
        } else {
            syncScope {
                borderAPI.resetWorldBorderToGlobal(bukkitPlayer)
            }
        }
    } }
}