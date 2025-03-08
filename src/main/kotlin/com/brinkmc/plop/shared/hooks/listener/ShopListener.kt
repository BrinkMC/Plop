package com.brinkmc.plop.shared.hooks.listener

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shop.shop.Shop
import com.brinkmc.plop.shop.shop.ShopType
import io.papermc.paper.event.block.BlockBreakBlockEvent
import io.papermc.paper.event.player.PlayerInsertLecternBookEvent
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.BlockFace
import org.bukkit.block.Chest
import org.bukkit.block.Lectern
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

class ShopListener(override val plugin: Plop): Addon, State, Listener {
    override suspend fun load() {}

    override suspend fun kill() {}

    @EventHandler(priority = EventPriority.MONITOR)
    suspend fun destroyShop(event: BlockBreakEvent) {
        val block = event.block

        if (block.type != Material.CHEST) {
            return
        }

        // This affects chests oh dear
        val chest = block.state as Chest

        val shopId = syncScope { // Needs to be thread-safe
             chest.persistentDataContainer.get(shops.handler.key, PersistentDataType.STRING)
        }

        if (shopId == null) {
            // Previous check to see if shop creation is occurring if the chest isn't a shop to begin with
            val isChoice = shops.creationHandler.isReserved(chest.location)

            if (isChoice) { // Do not destroy chest WHILE shop creation is occuring
                event.isCancelled = true
                return
            }

            return
        }

        UUID.fromString(shopId).shop() ?: return
        // This is a shop with valid data

        event.isCancelled = true // Cancel the event to prevent the chest from breaking
    }

    @EventHandler(priority = EventPriority.MONITOR)
    suspend fun preventConjoined(event: BlockPlaceEvent) {
        val block = event.block

        if (block.type != Material.CHEST) {
            return
        }

        val chest = block.state as Chest

        // Check surrounding blocks in all cardinal directions (NOT ABOVE OR BELOW)

        val north = chest.block.getRelative(BlockFace.NORTH)
        val south = chest.block.getRelative(BlockFace.SOUTH)
        val east = chest.block.getRelative(BlockFace.EAST)
        val west = chest.block.getRelative(BlockFace.WEST)

        val nearbyChests = listOf(north, south, east, west).mapNotNull {
            // Check if the block is a chest
            it.state as? Chest
        }

        for (nearby in nearbyChests) {
            val shopId = syncScope {
                nearby.persistentDataContainer.get(shops.handler.key, PersistentDataType.STRING)
            }

            if (shopId != null) {
                event.isCancelled = true
                return
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    suspend fun onShop(event: PlayerInteractEvent) {
        // Get the block
        val block = event.clickedBlock ?: return

        if (block.type != Material.CHEST) { // Shops are all chests
            return
        }

        event.isCancelled = true // Prematurely cancel the event for speed

        val chest = block.state as Chest // Cast as chest so that I can get TileState

        val shop = chest.toShop()

        val action = event.action == Action.RIGHT_CLICK_BLOCK || event.action == Action.LEFT_CLICK_BLOCK
        val sneak = event.player.isSneaking
        when { // Four possible permutations of interaction
            action && sneak && (shop == null) -> { // Trying to initiate?
                createShop(event, chest)
            }
            (shop == null) -> { // Shop doesn't exist
                event.isCancelled = false
                return
            }
            action && sneak -> { // It is a shop but they shouldn't sneak to access
                return
            }
            action -> { // Shop exists and player wants access (they aren't sneaking)
                viewShop(event, shop)
            }
            else -> { // idek what they're up to?
                return
            }
        }
    }

    // Only three outcomes, create or view x2 shop
    private suspend fun createShop(event: PlayerInteractEvent, chest: Chest) {
        // Get the player
        val player = event.player

        // Get the plot
        val plot = player.getCurrentPlot() ?: return

        // Check if the player is the owner of the plot
        if (!plot.owner.isPlayer(player)) {
            return
        }

        if (!player.hasPermission("plop.shop.create")) { // No permission to create a shop in the plot
            return
        }

        // Initiate player shop creation
        val shop = shops.creationHandler.initiateShopCreation(player, chest, plot.plotId, plot.type) ?: return
        shops.creationHandler.finaliseShop(player, shop)
        player.sendMiniMessage("shop.created")
    }

    private suspend fun viewShop(event: PlayerInteractEvent, shop: Shop) {
        // Get the player
        val player = event.player
        plugin.menus.shopMainMenu.open(player, shop)
    }
}