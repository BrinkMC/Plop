package com.brinkmc.plop.shop.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.factory.dto.Factory
import com.brinkmc.plop.plot.constant.PlotType
import com.brinkmc.plop.plot.dto.Plot
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.constant.MessageKey
import com.brinkmc.plop.shared.constant.ServiceResult
import com.brinkmc.plop.shared.constant.SoundKey
import com.brinkmc.plop.shared.util.LocationString.fullString
import com.brinkmc.plop.shared.util.LocationString.toLocation
import com.brinkmc.plop.shop.constant.ShopType
import com.brinkmc.plop.shop.dto.Shop
import com.brinkmc.plop.shop.dao.ShopCache
import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.Cache
import com.sksamuel.aedile.core.asLoadingCache
import com.sksamuel.aedile.core.expireAfterAccess
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.block.Chest
import org.bukkit.entity.Item
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

    // Getters

    suspend fun getShops(plotId: UUID): List<UUID> { // Get all shops which have plotId of some value
        return shopCache.getShops(plotId).map { it.id }
    }

    private suspend fun getShop(id: UUID): Shop? {
        return shopCache.getShop(id)
    }

    suspend fun getShopCount(plotId: UUID): Int {
        return getShops(plotId).size
    }

    suspend fun getShopIdFromLocation(location: Location): UUID? {
        return plugin.asyncScope {
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
        }
    }

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
        return getShop(id)?.location?.toLocation()
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

    suspend fun getShopOwnerDisplayName(shopId: UUID): String? {
        val ownerId = getShopOwnerId(shopId) ?: return null
        val plotType = plotService.getPlotType(ownerId) ?: return null
        return when (plotType) {
            PlotType.PERSONAL -> {
                val owner = playerService.getDisplayName(ownerId) ?: return null
                PlainTextComponentSerializer.plainText().serialize(owner)
            }

            PlotType.GUILD -> {
                hookService.guilds.getGuild(ownerId)?.name ?: return null
            }
        }
    }

    // Setters

    suspend fun createShop(
        playerId: UUID,
        location: Location,
        shopType: ShopType,
        item: ItemStack,
        quantity: Int,
        price: Double,
        open: Boolean,
        sellLimit: Int?
    ): ServiceResult {
        val shopId = UUID.randomUUID()

        val registered = registerChest(location, shopId)
        if (!registered) return ServiceResult.Failure(MessageKey.SHOP_CHEST_INVALID, SoundKey.FAILURE)

        val shop = Shop(
            shopId,
            location.fullString(),
            shopType,
            item,
            quantity,
            price,
            open,
            mutableListOf(),
            sellLimit ?: 0
        )

        shopCache.addShop(shop)
        return ServiceResult.Success(MessageKey.SHOP_CREATED_SUCCESS, SoundKey.SUCCESS)
    }

    suspend fun deleteShop(shopId: UUID): ServiceResult {
        val shop = getShop(shopId) ?: return ServiceResult.Failure(MessageKey.SHOP_NOT_EXIST, SoundKey.FAILURE)
        val location =
            getShopLocation(shopId) ?: return ServiceResult.Failure(MessageKey.SHOP_NOT_EXIST, SoundKey.FAILURE)

        // Drop all items onto floor around chest
        val quantity =
            getShopQuantity(shopId) ?: return ServiceResult.Failure(MessageKey.SHOP_NOT_EXIST, SoundKey.FAILURE)
        val item = getShopItem(shopId) ?: return ServiceResult.Failure(MessageKey.SHOP_NOT_EXIST, SoundKey.FAILURE)

        if (quantity > 0) {
            // can't close when not empty
            return ServiceResult.Failure(MessageKey.SHOP_NOT_EMPTY, SoundKey.FAILURE)
        }

        // Close all active sessions
        //TODO WIP
        shopAccessService.getOpenAccess(shopId).forEach {
        }
        unregisterChest(location)

        shopCache.deleteShop(shop)
        return ServiceResult.Success(MessageKey.SHOP_DELETED_SUCCESS, SoundKey.SUCCESS)
    }

    // Real world synchronous operations
    private suspend fun registerChest(location: Location, shopId: UUID): Boolean {
        return plugin.syncScope {
            val chest = location.block.state
            if (chest !is Chest) return@syncScope false
            chest.persistentDataContainer.set(
                key,
                PersistentDataType.STRING,
                shopId.toString()
            ) // Set the chest data to shop data
            chest.update()
            return@syncScope true
        }
    }

    private suspend fun unregisterChest(location: Location): Boolean {
        return plugin.syncScope {
            val chest = location.block.state
            if (chest !is Chest) return@syncScope false
            chest.persistentDataContainer.remove(key) // Remove the chest data
            chest.update()
            return@syncScope true
        }
    }

    suspend fun addTransaction(id: UUID, playerId: UUID, amount: Int, cost: Double) {
        val shop = getShop(id) ?: return
        shop.addTransaction(playerId, amount, cost)
    }
}

