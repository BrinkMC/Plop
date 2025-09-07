package com.brinkmc.plop.factory.dao

import com.brinkmc.plop.Plop
import com.brinkmc.plop.factory.dto.Factory
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.util.CoroutineUtils.async
import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.asLoadingCache
import com.sksamuel.aedile.core.expireAfterAccess
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
                databaseHandler.loadFactory(it)
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

    suspend fun getFactory(id: UUID): Factory? {
        return factoryMap.get(id)
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

    suspend fun  deleteFactory(factory: Factory) {
        plugin.asyncScope {
            factoryMap.invalidate(factory.id) // Deletes the plot from the cache
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

