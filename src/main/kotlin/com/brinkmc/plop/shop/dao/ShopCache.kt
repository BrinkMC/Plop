package com.brinkmc.plop.shop.dao

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.util.CoroutineUtils.async
import com.brinkmc.plop.shop.dto.Shop
import com.brinkmc.plop.shop.dao.DatabaseShop
import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.asLoadingCache
import com.sksamuel.aedile.core.expireAfterAccess
import kotlinx.coroutines.delay
import java.util.UUID
import kotlin.time.Duration.Companion.minutes

class ShopCache(override val plugin: Plop): Addon, State {

    private val databaseHandler = DatabaseShop(plugin)

    private val shopMap = Caffeine.newBuilder()
        .expireAfterAccess(30.minutes)
        .asLoadingCache<UUID, Shop?> {
            plugin.asyncScope {
                databaseHandler.loadShop(it)
            }
        } // SHOP ID -> SHOP

    override suspend fun load() {
        plugin.async { cacheSave() }// Get the task going
    }

    private suspend fun cacheSave() {
        while (true) {
            delay(5.minutes)
            shopMap.asMap().forEach { (id, shop) ->
                if (shop != null) {
                    databaseHandler.saveShop(shop)
                }
            }
        }
    }

    suspend fun getShops(): Map<UUID, Shop?> {
        return shopMap.asMap()
    }

    suspend fun getShop(shopId: UUID): Shop? {
        return shopMap.get(shopId)
    }

    suspend fun addShop(shop: Shop) {
        plugin.asyncScope {
            shopMap.invalidate(shop.id) // In case it was there before
            databaseHandler.saveShop(shop) // Adds the plot to the database
            shopMap.get(shop.id) {
                shop
            } // Adds the plot to the cache
        }
    }

    suspend fun deleteShop(shop: Shop) {
        plugin.asyncScope {
            shopMap.invalidate(shop.id) // Deletes the plot from the cache
            databaseHandler.deleteShop(shop) // Deletes the plot from the database
        }
    }

    override suspend fun kill() {
        shopMap.asMap().forEach { (id, shop) ->
            if (shop != null) {
                databaseHandler.saveShop(shop)
            }
        }
    }
}