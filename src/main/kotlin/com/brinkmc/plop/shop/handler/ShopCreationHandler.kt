package com.brinkmc.plop.shop.handler

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shop.shop.Shop
import com.github.benmanes.caffeine.cache.Caffeine
import com.noxcrew.interfaces.view.InterfaceView
import com.sksamuel.aedile.core.asCache
import com.sksamuel.aedile.core.asLoadingCache
import com.sksamuel.aedile.core.expireAfterAccess
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.bukkit.Location
import org.bukkit.block.Chest
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.get
import kotlin.time.Duration.Companion.minutes

class ShopCreationHandler(override val plugin: Plop): Addon, State {

    // Data structure to hold all shop creation session data
    data class ShopCreationSession(
        val shop: Shop,
        val completion: CompletableDeferred<Shop?>,
        val view: InterfaceView? = null
    )

    // Single cache for all shop creation sessions
    private val sessions = Caffeine.newBuilder()
        .expireAfterAccess(5.minutes)
        .asCache<UUID, ShopCreationSession>()

    // Helper function to get all reserved locations
    private suspend fun reservedLocations(): Set<Location> {
        return sessions.asMap().values.map { it.shop.location }.toSet()
    }

    override suspend fun load() {}

    override suspend fun kill() {
        sessions.invalidateAll()
    }

    suspend fun getShop(player: Player): Shop? {
        return sessions.getIfPresent(player.uniqueId)?.shop
    }

    suspend fun getSession(player: Player): ShopCreationSession? {
        return sessions.getIfPresent(player.uniqueId)
    }

    // Create a shop to begin with
    suspend fun initiateShopCreation(player: Player, chest: Chest, plotId: UUID, plotType: PlotType): Shop? {
        val playerId = player.uniqueId

        // Clean up any existing session
        sessions.invalidate(playerId)

        // Create new shop
        val shop = plugin.shops.handler.emptyShop(plotId, plotType)
        shop.setLocation(chest.location.clone())

        // Create new session
        val completion = CompletableDeferred<Shop?>()
        val session = ShopCreationSession(
            shop = shop,
            completion = completion
        )

        // Store the session
        sessions[playerId] = session

        // Open the menu and wait for completion
        val view = plugin.menus.shopInitCreateMenu.open(player, shop)
        sessions[playerId] = session.copy(view = view)

        return completion.await()
    }

    // Finalise shop creation and move to the permanent cache
    suspend fun finaliseShop(player: Player, shop: Shop) {
        val playerId = player.uniqueId

        val chest = shop.chest()
        val plot = shop.plotId.plot() ?: return

        // Register the shop
        plugin.logger.info("Adding shop to plot & handler")
        plugin.shops.handler.createShop(plot, shop, chest)
        plugin.logger.info("Added shop to database")


        // Clean up the session
        sessions.invalidate(playerId)
    }

    // Cancel shop creation
    suspend fun cancelShopCreation(player: Player) {
        val session = sessions.getIfPresent(player.uniqueId)
        if (session != null && !session.completion.isCompleted) {
            session.completion.complete(null)
        }
        sessions.invalidate(player.uniqueId)
    }

    // Check if player has a shop in creation
    suspend fun hasShopInCreation(player: Player): Boolean {
        return sessions.contains(player.uniqueId)
    }

    suspend fun isReserved(location: Location): Boolean {
        return reservedLocations().contains(location)
    }
}