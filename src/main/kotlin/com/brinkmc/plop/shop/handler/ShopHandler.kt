package com.brinkmc.plop.shop.handler

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shop.shop.Shop
import com.brinkmc.plop.shop.storage.ShopCache
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Chest
import org.bukkit.inventory.ItemStack
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
        return shopCache.getShops(plotId)?.toList() ?: listOf()
    }

    suspend fun getShop(shopId: UUID): Shop? {
        return shopCache.getShop(shopId)
    }

    suspend fun createShop(plot: Plot, shop: Shop, chest: Chest) {
        syncScope {
            chest.persistentDataContainer.set(key, PersistentDataType.STRING, shop.shopId.toString()) // Set the chest data to shop data
            chest.update()
        }
        shopCache.addShop(shop)
    }

    suspend fun deleteShop(shop: Shop) {
        shopCache.deleteShop(shop)
    }

    fun emptyShop(plotId: UUID, plotType: PlotType): Shop {
        return Shop( // Full of temporary values
            shopId = UUID.randomUUID(),
            plotId = plotId,
            plotType = plotType,
            _location = Location(null, 0.0, 0.0, 0.0),
            _item = ItemStack(Material.AIR),
            _quantity = 0,
            _sellPrice = -1.0f,
            _buyPrice = -1.0f,
            _buyLimit = -1,
            _open = false,
            _transaction = mutableListOf()
        )
    }


}
