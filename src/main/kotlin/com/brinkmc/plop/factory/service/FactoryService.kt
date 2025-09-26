package com.brinkmc.plop.factory.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.factory.constant.FactoryResult
import com.brinkmc.plop.factory.constant.FactoryType
import com.brinkmc.plop.factory.dao.FactoryCache
import com.brinkmc.plop.factory.dto.Factory
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.util.LocationString.fullString
import org.bukkit.Location
import java.util.UUID
import kotlin.math.PI

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

    private suspend fun getFactories(plotId: UUID): List<UUID> {
        return factoryCache.getFactories(plotId).map { it.id }
    }

    suspend fun getFactoryCount(plotId: UUID): Int {
        return getFactories(plotId).size
    }


    suspend fun placeFactory(playerId: UUID, location: Location, factoryType: FactoryType): FactoryResult {
        // Already presumes the item WAS a factory item

        // check if player is a plot owner
        val plotId = plotService.getPlotIdFromLocation(location) ?: return FactoryResult.NOT_IN_PLOT
        if (!plotService.getPlotMembers(plotId).contains(playerId)) {
            return FactoryResult.NOT_PLOT_OWNER
        }

        // Check if over the limit
        val factoryLimit = plotFactoryService.checkFactoryLimit(plotId)

        if (!factoryLimit) {
            return FactoryResult.OVER_LIMIT
        }
        val factoryId = UUID.randomUUID()

        // Not over limit, register the factory
        val factory = Factory(
            factoryId,
            location.fullString(),
            factoryType
        )

        factoryCache.addFactory(factory)

    }
}