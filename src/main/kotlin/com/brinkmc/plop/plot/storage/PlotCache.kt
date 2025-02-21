package com.brinkmc.plop.plot.storage

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.plot.storage.database.DatabasePlot
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import java.util.Objects
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.text.get

class PlotCache(override val plugin: Plop): Addon, State {

    private val databaseHandler = DatabasePlot(plugin)

    private val plotMap = ConcurrentHashMap<UUID, Deferred<Plot?>>()

    override suspend fun load() {
        cacheSave() // Get the task going
    }

    suspend fun cacheSave() {
        while (true) {
            plotMap.forEach { (_, deferred) ->
                val awaited = deferred.await()
                if (awaited != null) {
                    databaseHandler.save(awaited)
                }
            }
            delay(600_000) // 10 minutes
        }
    }

    suspend fun getPlot(plotId: UUID): Plot? {
        val key = plotId
        return asyncScope {
            if (!plotMap.containsKey(key)) {
                // Cache miss, create a new job
                plotMap[key] = async(Dispatchers.IO) {
                    databaseHandler.load(key)
                }
            }

            // Await suspends the current context until the value of the ``Deferred`` job is ready.
            val result = plotMap[key]?.await()
            if (result == null) {
                plotMap.remove(key) // Get rid of it because it's useless
            }
            result
        }
    }

    suspend fun addPlot(plot: Plot) {
        asyncScope {
            plotMap.remove(plot.plotId) // In case it was there before
            databaseHandler.create(plot) // Adds the plot to the database
            getPlot(plot.plotId) // Adds the plot to the cache
        }
    }

    suspend fun  deletePlot(plot: Plot) {
        asyncScope {
            databaseHandler.delete(plot) // Deletes the plot from the database
            plotMap.remove(plot.plotId)
        }
    }

    override suspend fun kill() {
        plotMap.forEach { (_, deferred) ->
            val awaited = deferred.await()
            if (awaited != null) {
                databaseHandler.save(awaited)
            }
        }
    }
}