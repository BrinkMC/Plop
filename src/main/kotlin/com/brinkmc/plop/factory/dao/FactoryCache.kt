package com.brinkmc.plop.factory.dao

import com.brinkmc.plop.Plop
import com.brinkmc.plop.factory.dto.Factory
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.util.CoroutineUtils.async
import com.brinkmc.plop.shared.util.LocationString.toLocation
import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.asLoadingCache
import com.sksamuel.aedile.core.expireAfterAccess
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.bukkit.Location
import java.util.UUID
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.time.Duration.Companion.minutes

class FactoryCache(override val plugin: Plop): Addon, State {

    private val databaseHandler = DatabaseFactory(plugin)

    private val factoryMap = Caffeine.newBuilder()
        .expireAfterAccess(30.minutes)
        .asLoadingCache<UUID, Factory?> {
            plugin.asyncScope {
                val factory = databaseHandler.loadFactory(it) ?: return@asyncScope null
                val location = factory.location.toLocation() ?: return@asyncScope null

                val plotId = plotService.getPlotIdFromLocation(location) ?: return@asyncScope factory
                locationCache.invalidate(plotId) // Invalidate the location cache for this plotId, as a factory has been loaded for it

                factory
            }
        }

    private val locationCache = Caffeine.newBuilder()
        .expireAfterAccess(30.minutes)
        .asLoadingCache<UUID, List<UUID>> {
            plugin.asyncScope {
                getFactories().filter {
                    val location = it.value?.location?.toLocation()!! // Let's hope no errors happen here
                    it.value?.location != null && plotService.getPlotIdFromLocation(location) == it
                }.map { it.key }
            }
        }


    override suspend fun load() {
        plugin.async { cacheSave() } // Get the task going
    }

    private suspend fun cacheSave() {
        while (true) {
            delay(5.minutes)
            factoryMap.asMap().forEach { (id, factory) ->
                if (factory != null) {
                    databaseHandler.saveFactory(factory)
                }
            }
        }
    }

    suspend fun getFactories(): Map<UUID, Factory?> {
        return factoryMap.asMap()
    }

    suspend fun getFactories(plotId: UUID): List<Factory> {
        val factoryIds = locationCache.get(plotId)
        return factoryIds.mapNotNull { getFactory(it) }
    }

    suspend fun getFactory(id: UUID): Factory? {
        return factoryMap.get(id)
    }

    suspend fun getFactory(location: Location): Factory? {
        // Get factories in plot and then filter from there
        val plotId = plotService.getPlotIdFromLocation(location) ?: return null
        return getFactories(plotId).firstOrNull {
            it.location.toLocation() == location
        }
    }

    suspend fun addFactory(factory: Factory) {
        plugin.asyncScope {
            factoryMap.invalidate(factory.id) // In case it was there before
            databaseHandler.saveFactory(factory) // Adds the factory to the database
            factoryMap.get(factory.id) {
                factory
            } // Adds the factory to the cache
        }
    }

    suspend fun deleteFactory(factory: Factory) {
        plugin.asyncScope {
            factoryMap.invalidate(factory.id) // Deletes the plot from the cache
            locationCache.invalidate(factory.id)
            databaseHandler.deleteFactory(factory) // Deletes the plot from the database
        }
    }

    override suspend fun kill() {
        factoryMap.asMap().forEach { (id, factory) ->
            if (factory != null) {
                databaseHandler.saveFactory(factory)
            }
        }
    }
}

