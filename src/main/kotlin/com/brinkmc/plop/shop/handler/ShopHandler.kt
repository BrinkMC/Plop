package com.brinkmc.plop.shop.handler

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shop.shop.Shop
import com.brinkmc.plop.shop.storage.ShopCache
import org.bukkit.NamespacedKey
import org.bukkit.block.Chest
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

class ShopHandler(override val plugin: Plop): Addon, State {

    private lateinit var shopCache: ShopCache

    val key = NamespacedKey(plugin, "shop")

    override suspend fun load() {
        logger.info("Loading Shop cache")
        shopCache = ShopCache(plugin)

        shopCache.load()
    }

    override suspend fun kill() {
        logger.info("Killing Shop cache")
        shopCache.kill()
    }

    suspend fun getShops(plotId: UUID): List<Shop> { // Get all shops which have plotId of some value
        return plots.handler.getPlotById(plotId)?.shop?.getShops()?.mapNotNull { getShop(it) }?.toList() ?: listOf()
    }

    suspend fun getShop(shopId: UUID): Shop? {
        return shopCache.getShop(shopId)
    }

    suspend fun createShop(plot: Plot, shop: Shop, chest: Chest) {
        syncScope {
            chest.persistentDataContainer.set(key, PersistentDataType.STRING, shop.shopId.toString()) // Set the chest data to shop data
            chest.update()
        }
        plot.shop.addShop(shop.shopId)
        shopCache.addShop(shop)
    }

    suspend fun deleteShop(shop: Shop) {
        shopCache.deleteShop(shop)
    }
}
