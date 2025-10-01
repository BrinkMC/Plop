package com.brinkmc.plop.factory.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.factory.constant.FactoryType
import com.brinkmc.plop.factory.dao.FactoryCache
import com.brinkmc.plop.factory.dto.Factory
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.constant.MessageKey
import com.brinkmc.plop.shared.constant.ServiceResult
import com.brinkmc.plop.shared.util.CoroutineUtils.async
import com.brinkmc.plop.shared.util.CoroutineUtils.sync
import com.brinkmc.plop.shared.util.LocationString.fullString
import com.brinkmc.plop.shared.util.type.MutexSet
import com.github.shynixn.mccoroutine.bukkit.ticks
import kotlinx.coroutines.delay
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.BlockType
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.Directional
import org.bukkit.block.data.Rotatable
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import java.util.*
import kotlin.math.roundToInt


class FactoryService(override val plugin: Plop): Addon, State {

    private val factoryCache: FactoryCache = FactoryCache(plugin)



    override suspend fun load() {
        logger.info("Loading factory cache")

        factoryCache.load()
    }

    override suspend fun kill() {
        logger.info("Killing factory cache")

        factoryCache.kill()
    }

    // Getters

    private suspend fun getFactory(factoryId: UUID): Factory? {
        return factoryCache.getFactory(factoryId)
    }

    suspend fun getFactories(plotId: UUID): List<UUID> {
        return factoryCache.getFactories(plotId).map { it.id }
    }

    suspend fun getFactoryIdFromLocation(location: Location): UUID? {
        return factoryCache.getFactory(location)?.id
    }

    suspend fun getFactoryCount(plotId: UUID): Int {
        return getFactories(plotId).size
    }



    suspend fun getFactoryType(factoryId: UUID): FactoryType? {
        return getFactory(factoryId)?.factoryType
    }

    suspend fun getFactoryLocation(factoryId: UUID): String? {
        return getFactory(factoryId)?.location
    }

    // Actions

    suspend fun placeFactory(playerId: UUID, location: Location, factoryType: FactoryType): ServiceResult {
        // Already presumes the item WAS a factory item

        // check if player is a plot owner
        val plotId = plotService.getPlotIdFromLocation(location) ?: return ServiceResult.Failure(MessageKey.NO_PLOT)

        if (!plotService.getPlotMembers(plotId).contains(playerId)) {
            return ServiceResult.Failure(MessageKey.NOT_OWNER)
        }

        // Check if over the limit
        val factoryLimit = plotFactoryService.checkFactoryLimit(plotId)

        if (!factoryLimit) {
            return ServiceResult.Failure(MessageKey.TOO_MANY_FACTORIES)
        }
        val factoryId = UUID.randomUUID()

        // Not over limit, register the factory
        val factory = Factory(
            factoryId,
            location.fullString(),
            factoryType
        )

        factoryCache.addFactory(factory)
        return ServiceResult.Success(MessageKey.PLACED_FACTORY_SUCCESS)
    }

    suspend fun destroyFactory(factoryId: UUID): ServiceResult {
        val factory = factoryCache.getFactory(factoryId) ?: return ServiceResult.Failure()

        factoryCache.deleteFactory(factory)

        return ServiceResult.Success(MessageKey.DESTROYED_FACTORY_SUCCESS)
    }
}