package com.brinkmc.plop.shop.handler

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shop.shop.Shop
import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.asCache
import com.sksamuel.aedile.core.asLoadingCache
import com.sksamuel.aedile.core.expireAfterAccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.bukkit.block.Chest
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.minutes

class ShopCreationHandler(override val plugin: Plop): Addon, State {

    // State flow for shop creation
    // 1. Player opens shop creation menu
    // 2. Player selects an item to sell + quantity of it
    // 3. Player selects a price to sell or buy
    // 4. Players can add quantity of items as stock
    // 5. Player can then choose also to buy or sell (thus both)
    // 6. Shop is completed

    override suspend fun load() {
    }

    override suspend fun kill() {
        temporaryShopMap.invalidateAll()
    }

    // Temporary shop cache map
    private val temporaryShopMap = Caffeine.newBuilder()
        .asCache<UUID, MutableStateFlow<Shop>>() // Player UUID

    // Get or create a shop state flow for a player
    suspend fun getOrCreateShop(player: Player, chest: Chest, plotId: UUID, plotType: PlotType): StateFlow<Shop> {
        return temporaryShopMap.get(player.uniqueId) {
            val shop = plugin.shops.handler.emptyShop(plotId, plotType)
            shop.setLocation(chest.location)
            MutableStateFlow(shop)
        }.asStateFlow()
    }

    // Get current shop snapshot
    suspend fun getShop(player: Player): Shop? {
        return temporaryShopMap.getIfPresent(player.uniqueId)?.value
    }

    // Finalise shop creation and move to the permanent cache
    suspend fun finaliseShop(player: Player) {
        val playerId = player.uniqueId
        val shop = temporaryShopMap.getIfPresent(playerId)?.value ?: return

        val chest = shop.chest
        val plot = shop.plotId.plot() ?: return
        // Register the shop in the actual shop handler
        logger.info("Adding shop to plot & handler")
        shops.handler.createShop(plot, shop, chest)

        // Clean up temporary shop
        temporaryShopMap.invalidate(playerId)
    }

    // Cancel shop creation
    fun cancelShopCreation(player: Player) {
        temporaryShopMap.invalidate(player.uniqueId)
    }

    // Check if player has a shop in creation
    suspend fun hasShopInCreation(player: Player): Boolean {
        return temporaryShopMap.contains(player.uniqueId)
    }

    suspend fun subscribe(player: Player, onUpdate: (Shop) -> Unit) {
        val shopFlow = temporaryShopMap.getIfPresent(player.uniqueId) ?: return
        plugin.async {
            shopFlow.collect { shop ->
                onUpdate(shop)
            }
        }
    }
}