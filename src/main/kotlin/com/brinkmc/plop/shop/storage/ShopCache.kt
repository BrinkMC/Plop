package com.brinkmc.plop.shop.storage

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.plot.storage.database.DatabasePlot
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shop.shop.Shop
import com.brinkmc.plop.shop.storage.database.DatabaseShop
import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.asCache
import com.sksamuel.aedile.core.expireAfterAccess
import kotlinx.coroutines.delay
import java.util.UUID
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.time.Duration.Companion.minutes

class ShopCache(override val plugin: Plop): Addon, State {

    private val databaseHandler = DatabaseShop(plugin)

    private val shopMap = Caffeine.newBuilder()
        .expireAfterAccess(30.minutes)
        .asCache<UUID, Shop?>()

    override suspend fun load() {
        cacheSave() // Get the task going
    }

    private suspend fun cacheSave() {
        while (true) {
            delay(5.minutes)
            asyncScope { shopMap.asMap().forEach { (id, shop) ->
                if (shop != null) {
                    databaseHandler.save(shop)
                }
            } }
        }
    }

    suspend fun getShop(shopId: UUID): Shop? {
        return shopMap.get(shopId) {
            asyncScope {
                databaseHandler.load(shopId)
            }
        }
    }

    suspend fun addShop(shop: Shop) {
        asyncScope {
            shopMap.invalidate(shop.shopId) // In case it was there before
            databaseHandler.create(shop) // Adds the plot to the database
            shopMap.get(shop.shopId) {
                shop
            } // Adds the plot to the cache
        }
    }

    suspend fun deleteShop(shop: Shop) {
        asyncScope {
            shopMap.invalidate(shop.shopId) // Deletes the plot from the cache
            databaseHandler.delete(shop) // Deletes the plot from the database
        }
    }

    override suspend fun kill() {
        asyncScope { shopMap.asMap().forEach { (id, shop) ->
            if (shop != null) {
                databaseHandler.save(shop)
            }
        } }
    }
}