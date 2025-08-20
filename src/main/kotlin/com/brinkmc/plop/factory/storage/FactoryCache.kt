package com.brinkmc.plop.factory.storage

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.plot.storage.database.DatabasePlot
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.asLoadingCache
import com.sksamuel.aedile.core.expireAfterAccess
import kotlinx.coroutines.delay
import java.util.UUID
import kotlin.time.Duration.Companion.minutes

class FactoryCache(override val plugin: Plop): Addon, State {

    private val databaseHandler = DatabasePlot(plugin)

    private val plotMap = Caffeine.newBuilder()
        .expireAfterAccess(30.minutes)
        .asLoadingCache<UUID, Plot?> {
            asyncScope {
                databaseHandler.load(it)
            }
        }

    override suspend fun load() {
        plugin.async { cacheSave() } // Get the task going
    }

    private suspend fun cacheSave() {
        while (true) {
            delay(5.minutes)
            plotMap.asMap().forEach { (id, plot) ->
                if (plot != null) {
                    databaseHandler.save(plot)
                }
            }
        }
    }

    suspend fun getPlot(plotId: UUID): Plot? {
        return plotMap.get(plotId)
    }

    suspend fun addPlot(plot: Plot) {
        asyncScope {
            plotMap.invalidate(plot.plotId) // In case it was there before
            databaseHandler.create(plot) // Adds the plot to the database
            plotMap.get(plot.plotId) {
                plot
            } // Adds the plot to the cache
        }
    }

    suspend fun  deletePlot(plot: Plot) {
        asyncScope {
            plotMap.invalidate(plot.plotId) // Deletes the plot from the cache
            databaseHandler.delete(plot) // Deletes the plot from the database
        }
    }

    override suspend fun kill() {
        plotMap.asMap().forEach { (id, plot) ->
            if (plot != null) {
                databaseHandler.save(plot)
            }
        }
    }
}