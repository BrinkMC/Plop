package com.brinkmc.plop.plot.dao

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.dto.Plot
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.util.CoroutineUtils.async
import com.brinkmc.plop.shared.util.LocationString.fullString
import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.asCache
import com.sksamuel.aedile.core.asLoadingCache
import com.sksamuel.aedile.core.expireAfterAccess
import kotlinx.coroutines.delay
import org.bukkit.Location
import java.util.UUID
import kotlin.collections.mapNotNull
import kotlin.collections.set
import kotlin.time.Duration.Companion.minutes

class PlotCache(override val plugin: Plop): Addon, State {

    private val databaseHandler = DatabasePlot(plugin)

    private val plotMap = Caffeine.newBuilder()
        .expireAfterAccess(30.minutes)
        .asLoadingCache<UUID, Plot?> {
            plugin.asyncScope {
                databaseHandler.loadPlot(it)
            }
        }

    private val locationCache = Caffeine.newBuilder()
        .asCache<String, UUID>()

    override suspend fun load() {
        plugin.async { cacheSave() } // Get the task going
    }

    private suspend fun cacheSave() {
        while (true) {
            delay(5.minutes)
            plotMap.asMap().forEach { (id, plot) ->
                if (plot != null) {
                    databaseHandler.savePlot(plot)
                }
            }
        }
    }

    suspend fun getPlots(): Map<UUID, Plot?> {
        return plotMap.asMap()
    }

    suspend fun getPlot(plotId: UUID): Plot? {
        return plotMap.get(plotId)
    }

    suspend fun addPlot(plot: Plot) {
        plugin.asyncScope {
            plotMap.invalidate(plot.id) // In case it was there before
            databaseHandler.savePlot(plot) // Adds the plot to the database
            plotMap.get(plot.id) {
                plot
            } // Adds the plot to the cache
        }
    }

    suspend fun  deletePlot(plot: Plot) {
        plugin.asyncScope {
            plotMap.invalidate(plot.id) // Deletes the plot from the cache
            databaseHandler.deletePlot(plot) // Deletes the plot from the database
        }
    }

    override suspend fun kill() {
        plotMap.asMap().forEach { (id, plot) ->
            if (plot != null) {
                databaseHandler.savePlot(plot)
            }
        }
    }

    suspend fun getPlotId(location: Location): UUID? { return plugin.asyncScope {
        val locationKey = location.fullString(false)
        val cachedPlotId = locationCache.getIfPresent(locationKey)

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
}
