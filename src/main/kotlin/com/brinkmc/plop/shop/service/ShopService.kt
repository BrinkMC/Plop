package com.brinkmc.plop.shop.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.dto.Plot
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shop.constant.ShopType
import com.brinkmc.plop.shop.dto.Shop
import com.brinkmc.plop.shop.dao.ShopCache
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.block.Chest
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

class ShopService(override val plugin: Plop): Addon, State {

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

    private suspend fun getShops(plotId: UUID): Map<UUID,Shop?> { // Get all shops which have plotId of some value
        return shopCache.getShops()
    }

    private suspend fun getShop(id: UUID): Shop? {
        return shopCache.getShop(id)
    }

    suspend fun getShopIdFromLocation(location: Location): UUID? { return plugin.asyncScope {
        val chest = location.block.state
        if (chest !is Chest) return@asyncScope null
        val shopIdString = chest.persistentDataContainer.get(
            key, PersistentDataType.STRING
        ) ?: return@asyncScope null

        val shopId = try {
            UUID.fromString(shopIdString)
        } catch (e: IllegalArgumentException) {
            return@asyncScope null
        }

        return@asyncScope shopId
    } }

    suspend fun getShopItem(id: UUID): ItemStack? {
        return getShop(id)?.item?.let { ItemStack(it) }
    }

    suspend fun getShopType(id: UUID): ShopType? {
        return getShop(id)?.shopType
    }

    suspend fun getShopQuantity(id: UUID): Int? {
        return getShop(id)?.quantity
    }

    suspend fun getShopBalance(id: UUID): Double? {
        val shopLocation = getShop(id)?.location ?: return null
        val plotId = plotService.getPlotFromLocation(shopLocation) ?: return null
        val plotBalance = plotService.getPlotBalance(plotId)
        return plotBalance
    }

    suspend fun getShopPrice(id: UUID): Double? {
        return getShop(id)?.price
    }

    suspend fun getShopSellLimit(id: UUID): Int? {
        if (getShopType(id) != ShopType.BUY) return null // no way buy limit for SELL SHOP
        return getShop(id)?.buyLimit
    }




    suspend fun createShop(plot: Plot, shop: Shop, chest: Chest) {
        syncScope {
            chest.persistentDataContainer.set(key, PersistentDataType.STRING, shop.shopId.toString()) // Set the chest data to shop data
            chest.update()
        }
        asyncScope {
            shopCache.addShop(shop)
        }

    }

    suspend fun deleteShop(shop: Shop) {
        val chest = shop.chest()
        syncScope {
            chest.persistentDataContainer.remove(key) // Set the chest data to shop data
            chest.update()
        }
        asyncScope {
            shopCache.deleteShop(shop)
        }
    }



}
