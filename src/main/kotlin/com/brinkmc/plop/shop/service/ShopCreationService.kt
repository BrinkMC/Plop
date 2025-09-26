package com.brinkmc.plop.shop.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.util.LocationString.fullString
import com.brinkmc.plop.shared.util.LocationString.toLocation
import com.brinkmc.plop.shop.constant.ShopCreationResult
import com.brinkmc.plop.shop.constant.ShopType
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
import org.bukkit.inventory.ItemStack
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
        return reservedLocations().contains(location) && getSession(playerId)?.location != location.fullString()
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

    // Getters

    fun getShopType(playerId: UUID): ShopType? {
        val session = getSession(playerId) ?: return null
        return session.shopType
    }

    fun getItem(playerId: UUID): ItemStack? {
        val session = getSession(playerId) ?: return null
        return session.item
    }

    fun getQuantity(playerId: UUID): Int? {
        val session = getSession(playerId) ?: return null
        return session.quantity
    }

    fun getPrice(playerId: UUID): Double? {
        val session = getSession(playerId) ?: return null
        return session.price
    }

    fun getOpen(playerId: UUID): Boolean? {
        val session = getSession(playerId) ?: return null
        return session.open
    }

    fun getSellLimit(playerId: UUID): Int? {
        val session = getSession(playerId) ?: return null
        return session.sellLimit
    }

    fun getLocation(playerId: UUID): Location? {
        val session = getSession(playerId) ?: return null
        return session.location.toLocation()
    }

    // Setters

    suspend fun setShopType(playerId: UUID, shopType: ShopType) {
        val session = getSession(playerId) ?: return
        session.setShopType(shopType)
    }

    suspend fun setItem(playerId: UUID, item: ItemStack) {
        val session = getSession(playerId) ?: return
        session.setItem(item)
    }

    suspend fun setQuantity(playerId: UUID, quantity: Int) {
        val session = getSession(playerId) ?: return
        session.setQuantity(quantity)
    }

    suspend fun setPrice(playerId: UUID, price: Double) {
        val session = getSession(playerId) ?: return
        session.setPrice(price)
    }

    suspend fun setOpen(playerId: UUID, open: Boolean) {
        val session = getSession(playerId) ?: return
        session.setOpen(open)
    }

    suspend fun setSellLimit(playerId: UUID, sellLimit: Int) {
        val session = getSession(playerId) ?: return
        session.setSellLimit(sellLimit)
    }


    suspend fun initialiseShopCreation(playerId: UUID, location: Location): ShopCreationResult {

        // check is reserved
        if (isReserved(playerId, location)) {
            return ShopCreationResult.RESERVED_CHEST
        }

        // Check if player already has a session, if so cancel
        var session = getSession(playerId)
        if ((session != null && location.fullString() != session.location) || (session == null)) {
            val id = UUID.randomUUID()
            session = ShopCreation(
                id,
                location.fullString(),
                null,
                null,
                null,
                null,
                null,
                null
            )
        }

        // Check if they have reached shop limit in plot
        val plotId = plotService.getPlotIdFromLocation(location) ?: return ShopCreationResult.NOT_IN_PLOT
        val shopLimit = plotShopService.checkShopLimit(plotId)

        if (!shopLimit) {
            return ShopCreationResult.OVER_LIMIT
        }

        setSession(playerId, session)
        return ShopCreationResult.SUCCESS
    }

    suspend fun finaliseShopCreation(playerId: UUID): ShopCreationResult {

        val location = getLocation(playerId) ?: return ShopCreationResult.FAILURE
        // Final checks
        val plotId = plotService.getPlotIdFromLocation(location) ?: return ShopCreationResult.NOT_IN_PLOT
        val shopLimit = plotShopService.checkShopLimit(plotId)

        if (!shopLimit) {
            return ShopCreationResult.OVER_LIMIT
        }
        // Someone could've registered a shop at this location in the meantime / triple check
        if (isReserved(playerId, location)) {
            return ShopCreationResult.RESERVED_CHEST
        }

        // Check all fields are filled except sell limit (only for sell shops)

        val shopType = getShopType(playerId) ?: return ShopCreationResult.INCOMPLETE
        val item = getItem(playerId) ?: return ShopCreationResult.INCOMPLETE
        val quantity = getQuantity(playerId) ?: return ShopCreationResult.INCOMPLETE
        val price = getPrice(playerId) ?: return ShopCreationResult.INCOMPLETE
        val open = getOpen(playerId) ?: return ShopCreationResult.INCOMPLETE
        val sellLimit = if (shopType == ShopType.BUY) {
            getSellLimit(playerId) ?: return ShopCreationResult.INCOMPLETE
        } else {
            null
        }

        // Register the shop
        plugin.logger.info("Adding shop to plot & handler")
        shopService.createShop(
            playerId,
            location,
            shopType,
            item,
            quantity,
            price,
            open,
            sellLimit
        ) // Pass along information
        plugin.logger.info("Added shop to database")

        // Clean up the session
        removeSession(playerId)
        return ShopCreationResult.SUCCESS
    }
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