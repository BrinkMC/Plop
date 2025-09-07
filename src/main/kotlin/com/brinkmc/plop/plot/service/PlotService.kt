package com.brinkmc.plop.plot.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.dto.Plot
import com.brinkmc.plop.plot.dao.PlotCache
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.hook.api.Locals.world
import com.brinkmc.plop.shared.util.LocationString.fullString
import com.brinkmc.plop.plot.constant.PlotType
import com.brinkmc.plop.shop.dto.Shop
import com.github.yannicklamprecht.worldborder.api.WorldBorderApi
import org.bukkit.Location
import org.bukkit.World
import java.util.*

class PlotService(override val plugin: Plop): Addon, State  {

    private val plotCache: PlotCache = PlotCache(plugin) // Cache of all plots
    private val locationCache: HashMap<String, UUID> = hashMapOf()

     // Late init as it is not available on startup

    override suspend fun load() { plugin.syncScope {
        plotCache.load()

        borderAPI = server.servicesManager.getRegistration<WorldBorderApi?>(WorldBorderApi::class.java)?.provider ?: run {
            logger.error("Failed to get WorldBorderAPI")
            return@syncScope
        }
    }}


    override suspend fun kill() {
        plotCache.kill()
    }

    fun getPlotWorld(plotType: PlotType): World {
        return configService.plotConfig.getPlotWorld(plotType).world() ?: throw RuntimeException("Plot world not found")
    }

    fun isPlotWorld(world: World): Boolean {
        return listOfNotNull(
            configService.plotConfig.getPlotWorld(PlotType.PERSONAL).world(),
            configService.plotConfig.getPlotWorld(PlotType.GUILD).world()
        ).contains(world)
    }

    private suspend fun getPlots(playerId: UUID): Map<UUID,Plot?> { // Get all shops which have plotId of some value
        return plotCache.getPlots().filter {
            it.value?.id == playerId || it.value?.id == hookService.guilds.getGuildFromPlayer(playerId)
        }
    }

    private suspend fun getPlot(plotId: UUID): Plot? {
        return plotCache.getPlot(plotId)
    }

    suspend fun hasGuildPlot(playerId: UUID): Boolean {
        val guild = hookService.guilds.getGuildFromPlayer(playerId)?.id ?: return false
        return getPlot(guild) != null
    }

    suspend fun hasPersonalPlot(playerId: UUID): Boolean {
        return getPlot(playerId) != null
    }

    suspend fun getPlotIdFromLocation(location: Location): UUID? { return plugin.asyncScope {
        val locationKey = location.fullString(false)
        val cachedPlotId = locationCache[locationKey]
        if (cachedPlotId != null) {
            return@asyncScope cachedPlotId
        }

        val worldGuardRegions = plugin.hookService.worldGuard.getRegions(location)

        if (worldGuardRegions.size != 1) {
            return@asyncScope null
        }
        val regionIdString = worldGuardRegions.first()?.id ?: return@asyncScope null
        val plotId = UUID.fromString(regionIdString)

        locationCache[locationKey] = plotId
        return@asyncScope plotId
    } }















    suspend fun addPlot(plot: Plot) {
        plotCache.addPlot(plot)
    }

    suspend fun deletePlot(plot: Plot) {
        plotCache.deletePlot(plot)
    }
}