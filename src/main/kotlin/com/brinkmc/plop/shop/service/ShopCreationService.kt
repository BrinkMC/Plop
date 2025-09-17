package com.brinkmc.plop.shop.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shop.constant.ShopCreationResult
import com.brinkmc.plop.shop.dto.Shop
import com.brinkmc.plop.shop.dto.ShopCreation
import com.github.benmanes.caffeine.cache.Caffeine
import com.noxcrew.interfaces.view.InterfaceView
import com.sksamuel.aedile.core.asCache
import com.sksamuel.aedile.core.expireAfterAccess
import kotlinx.coroutines.CompletableDeferred
import org.bukkit.Location
import org.bukkit.block.Chest
import org.bukkit.entity.Player
import java.util.UUID
import kotlin.time.Duration.Companion.minutes

class ShopCreationService(override val plugin: Plop): Addon, State {

    // Single cache for all shop creation sessions
    private val playerToShopTracker = hashMapOf<UUID, ShopCreation>()

    // Helper function to get all reserved locations
    private suspend fun reservedLocations(): Set<Location> {
        return playerToShopTracker.values.map { it.location }.toSet()
    }

    private suspend fun isReserved(playerId: UUID, location: Location): Boolean {
        return reservedLocations().contains(location) && getSession(playerId)?.location != location
    }

    private fun setSession(playerId: UUID, session: ShopCreation) {
        playerToShopTracker[playerId] = session
    }

    private fun getSession(playerId: UUID): ShopCreation? {
        return playerToShopTracker[playerId]
    }

    private fun removeSession(playerId: UUID) {
        playerToShopTracker.remove(playerId)
    }

    suspend fun initialiseShopCreation(playerId: UUID, location: Location): ShopCreationResult {

        // check is reserved
        if (isReserved(playerId, location)) {
            return ShopCreationResult.RESERVED_CHEST
        }

        // Check if player already has a session, if so cancel
        var session = getSession(playerId)
        if ((session != null && location != session.location) || (session == null))  {
            val id = UUID.randomUUID()
            session = ShopCreation(
                id,
                location,
                null,
                null,
                null,
                null,
                null,
                null
            )
        }

        // Check if they have reached shop limit in plot
        val plotId = plotService.getPlotIdFromLocation(location)
        val plotShopLimit = plotShopService.getShopLimit(plotId) ?: return ShopCreationResult.MAX_PLOT_LIMIT_REACHED
        val currentShopCount = plotShopService.getShopCount(plotId) ?: return ShopCreationResult.MAX_PLOT_LIMIT_REACHED

        if (plotShopLimit <= currentShopCount) {
            return ShopCreationResult.MAX_PLOT_LIMIT_REACHED
        }

        setSession(playerId, session)
        return ShopCreationResult.SUCCESS
    }

//    suspend fun getShop(player: Player): Shop? {
//        return sessions.getIfPresent(player.uniqueId)?.shop
//    }
//
//    suspend fun getSession(player: Player): ShopCreationSession? {
//        return sessions.getIfPresent(player.uniqueId)
//    }
//
//    // Create a shop to begin with
//    suspend fun initiateShopCreation(player: Player, chest: Chest, plotId: UUID, plotType: PlotType): Shop? {
//        val playerId = player.uniqueId
//
//        // Clean up any existing session
//        sessions.invalidate(playerId)
//
//        // Create new shop
//        val shop = plugin.shops.handler.emptyShop(plotId, plotType)
//        shop.setLocation(chest.location.clone())
//
//        // Create new session
//        val completion = CompletableDeferred<Shop?>()
//        val session = ShopCreationSession(
//            shop = shop,
//            completion = completion
//        )
//
//        // Store the session
//        sessions[playerId] = session
//
//        // Open the menu and wait for completion
//        val view = plugin.menus.shopInitCreateMenu.open(player, shop)
//        sessions[playerId] = session.copy(view = view)
//
//        return completion.await()
//    }
//
//    // Finalise shop creation and move to the permanent cache
//    suspend fun finaliseShop(player: Player, shop: Shop) {
//        val playerId = player.uniqueId
//
//        val chest = shop.chest()
//        val plot = shop.plotId.plot() ?: return
//
//        // Register the shop
//        plugin.logger.info("Adding shop to plot & handler")
//        plugin.shops.handler.createShop(plot, shop, chest)
//        plugin.logger.info("Added shop to database")
//
//
//        // Clean up the session
//        sessions.invalidate(playerId)
//    }
//
//    // Cancel shop creation
//    suspend fun cancelShopCreation(player: Player) {
//        val session = sessions.getIfPresent(player.uniqueId)
//        if (session != null && !session.completion.isCompleted) {
//            session.completion.complete(null)
//        }
//        sessions.invalidate(player.uniqueId)
//    }
//
//    // Check if player has a shop in creation
//    suspend fun hasShopInCreation(player: Player): Boolean {
//        return sessions.contains(player.uniqueId)
//    }

    override suspend fun load() {}

    override suspend fun kill() {
        sessions.invalidateAll()
    }
}