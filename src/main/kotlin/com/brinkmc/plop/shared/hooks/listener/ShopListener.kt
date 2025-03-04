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
import org.bukkit.block.Chest
import org.bukkit.block.Lectern
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

class ShopListener(override val plugin: Plop): Addon, State, Listener {
    override suspend fun load() {}

    override suspend fun kill() {}

    private val key = NamespacedKey(plugin, "shop")

    @EventHandler(priority = EventPriority.MONITOR)
    suspend fun destroyShop(event: BlockBreakEvent) {
        val block = event.block

        if (block.type != Material.CHEST) {
            return
        }

        // This affects chests oh dear
        val chest = block.state as Chest

        val shopId = chest.persistentDataContainer.get(key, PersistentDataType.STRING)

        if (shopId == null) {
            // Previous check to see if shop creation is occurring if the chest isn't a shop to begin with
            val isChoice = plugin.menus.shopCreationMenu.isChoice(chest.location)

            if (isChoice) { // Do not destroy chest WHILE shop creation is occuring
                event.isCancelled = true
                return
            }
            return // Return anyway because it's useless to us
        }

        UUID.fromString(shopId).shop() ?: return
        // This is a shop with valid data

        event.isCancelled = true // Cancel the event to prevent the chest from breaking
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

        val shopId = chest.persistentDataContainer.get(key, PersistentDataType.STRING)

        val shop = if (shopId != null) { // Not a shop
            shops.handler.getShop(UUID.fromString(shopId))
        }
        else {
            null
        }

        val action = event.action == Action.RIGHT_CLICK_BLOCK || event.action == Action.LEFT_CLICK_BLOCK
        val sneak = event.player.isSneaking
        when { // Four possible permutations of interaction
            action && sneak && (shop == null) -> { // Trying to initiate?
                createShop(event, chest)
            }
            action && (shop == null) -> { // Shop doesn't exist
                event.isCancelled = false
                return
            }
            (shop == null) -> { // Not a shop and they're not trying to make one!
                event.isCancelled = false
                return
            }
            action && sneak -> { // It is a shop but they shouldn't sneak to access
                return
            }
            action -> { // Shop exists and player wants access (they aren't sneaking)
                if (shop.owner.isPlayer(event.player)) { // It is the owner
                    viewShopOwner(event, shop)
                }
                else { // It is a customer
                    viewShopCustomer(event, shop)
                }
            }
            else -> { // idek what they're up to?
                return
            }
        }
    }

    // Only three outcomes, create or view x2 shop
    suspend fun createShop(event: PlayerInteractEvent, chest: Chest) {
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
        val shop = plugin.menus.shopCreationMenu.requestChoice(player, event.clickedBlock?.location) ?: return  // No shop was created

        shops.handler.createShop(shop)
        plot.shop.addShop(shop.shopId)
        chest.persistentDataContainer.set(key, PersistentDataType.STRING, shop.shopId.toString())
        player.sendMiniMessage("shop.created")
    }

    suspend fun viewShopCustomer(event: PlayerInteractEvent, shop: Shop) {
        // Get the player
        val player = event.player

        // Open the shop customer menu
        if (!shop.open) {
            player.sendMiniMessage("shop.closed")
            return
        }

        plugin.menus.shopClientMenu.open(player)
    }

    suspend fun viewShopOwner(event: PlayerInteractEvent, shop: Shop) {
        // Get the player
        val player = event.player

        if (!player.hasPermission("plop.shop.owner")) {
            return
        }

        // Open the shop owner menu
        plugin.menus.shopOwnerMenu.open(player)
    }
}