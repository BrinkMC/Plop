package com.brinkmc.plop.shop.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.factory.dto.Factory
import com.brinkmc.plop.plot.dto.Plot
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shop.constant.ShopType
import com.brinkmc.plop.shop.dto.Shop
import com.brinkmc.plop.shop.dao.ShopCache
import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.Cache
import com.sksamuel.aedile.core.asLoadingCache
import com.sksamuel.aedile.core.expireAfterAccess
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.block.Chest
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.HashMap
import java.util.UUID
import kotlin.time.Duration.Companion.minutes

class ShopService(override val plugin: Plop): Addon, State {

    private val shopCache: ShopCache = ShopCache(plugin)


    val key = NamespacedKey(plugin, "shop")

    override suspend fun load() {
        logger.info("Loading Shop cache")

        shopCache.load()
    }

    override suspend fun kill() {
        logger.info("Killing Shop cache")
        shopCache.kill()
    }

    private fun getShops(plotId: UUID): List<UUID> { // Get all shops which have plotId of some value
        return getShops(plotId)
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

    suspend fun setShopQuantity(id: UUID, quantity: Int) {
        getShop(id)?.setQuantity(quantity)
    }

    suspend fun getShopLocation(id: UUID): Location? {
        return getShop(id)?.location
    }

    suspend fun getShopPrice(id: UUID): Double? {
        return getShop(id)?.price
    }

    suspend fun getShopSellLimit(id: UUID): Int? {
        if (getShopType(id) != ShopType.BUY) return null // no way buy limit for SELL SHOP
        return getShop(id)?.sellLimit
    }

    suspend fun getShopOwnerId(shopId: UUID): UUID? {
        val shopLocation = getShopLocation(shopId) ?: return null
        val plotId = plotService.getPlotIdFromLocation(shopLocation) ?: return null
        return plotId
    }

    suspend fun addTransaction(id: UUID, playerId: UUID, amount: Int, cost: Double) {
        val shop = getShop(id) ?: return
        shop.addTransaction(playerId, amount, cost)
    }





//    suspend fun createShop(plot: Plot, shop: Shop, chest: Chest) {
//        syncScope {
//            chest.persistentDataContainer.set(key, PersistentDataType.STRING, shop.shopId.toString()) // Set the chest data to shop data
//            chest.update()
//        }
//        asyncScope {
//            shopCache.addShop(shop)
//        }
//
//    }
//
//    suspend fun deleteShop(shop: Shop) {
//        val chest = shop.chest()
//        syncScope {
//            chest.persistentDataContainer.remove(key) // Set the chest data to shop data
//            chest.update()
//        }
//        asyncScope {
//            shopCache.deleteShop(shop)
//        }
//    }



}
