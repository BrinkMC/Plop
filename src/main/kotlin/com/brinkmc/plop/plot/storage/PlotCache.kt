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

    private val plotMap = ConcurrentHashMap<PlotKey, Deferred<Plot?>>()

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

    suspend fun getPlotById(plotId: UUID): Plot? {
        val key = PlotKey(plotId = plotId)
        return asyncScope {
            if (!plotMap.containsKey(key)) {
                // Cache miss, create a new job
                plotMap[key] = async(Dispatchers.IO) {
                    databaseHandler.load(key)
                }
            }

            // Await suspends the current context until the value of the ``Deferred`` job is ready.
            plotMap[key]?.await()
        }
    }

    suspend fun getPlotByOwner(ownerId: UUID?): Plot? {
        val key = PlotKey(ownerId = ownerId)
        return asyncScope {
            if (!plotMap.containsKey(key)) {
                // Cache miss, create a new job
                plotMap[key] = async(Dispatchers.IO) {
                    databaseHandler.load(key)
                }
            }

            // Await suspends the current context until the value of the ``Deferred`` job is ready.
            plotMap[key]?.await()
        }
    }

    suspend fun addPlot(plot: Plot) {
        asyncScope {
            databaseHandler.create(plot) // Adds the plot to the database, can be lazy called later
        }
    }

    suspend fun deletePlot(plot: Plot) {
        asyncScope {
            databaseHandler.delete(plot) // Deletes the plot from the database
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

/*
Plot key so that I can find things fast in O(1) time

Plot can either have
(plotId & ownerId) or (plotId & guildId)
 */

data class PlotKey(
    val plotId: UUID? = null,
    val ownerId: UUID? = null
) {
    // Literally overrides the == sign for PlotKeys lol
    override fun equals(other: Any?): Boolean {
        if (this === other) return true // Check if both references point to the same object
        if (javaClass != other?.javaClass) return false // Check if the object is the same as PlotKey
        other as PlotKey // Cast to PlotKey because it's the same, and we can treat it as such
        return ( plotId == other.plotId || ownerId == other.ownerId ) // Finally compare if the two are the same
    }

    override fun hashCode(): Int {
        return Objects.hash(plotId, ownerId) // Create hash from all 2 things put together
    }
}
