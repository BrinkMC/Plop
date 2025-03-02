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

    val key = NamespacedKey(plugin, "shop")

    @EventHandler(priority = EventPriority.MONITOR)
    suspend fun destroyShop(event: BlockBreakEvent) {
        val block = event.block

        if (block.type != Material.CHEST) {
            return
        }

        val chest = block.state as Chest


        val player = event.player

        val plot = player.getCurrentPlot() ?: return


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
        val shop = shops.handler.getShop(UUID.fromString(shopId))

        val action = event.action
        val sneak = event.player.isSneaking
        when { // Four possible permutations of interaction
            action == Action.RIGHT_CLICK_BLOCK && sneak -> {
                if (shop == null) {
                    createShop(event, chest)
                } else {
                    viewShopOwner(event, shop)
                }
            }
            action == Action.RIGHT_CLICK_BLOCK -> {
                if (shop == null) { // Allow the chest to be opened, it is just regular chest
                    event.isCancelled = false
                    return
                } else { // View the shop
                    if (shop.owner.isPlayer(event.player)) { // If the player is the owner
                        viewShopOwner(event, shop)
                    }
                    else { // It is a customer
                        viewShopCustomer(event, shop)
                    }
                }
            }
            // Mirror action for left click
            action == Action.LEFT_CLICK_BLOCK && sneak -> {
                if (shop == null) {
                    createShop(event, chest)
                } else {
                    viewShopOwner(event, shop)
                }
            }
            action == Action.LEFT_CLICK_BLOCK -> {
                if (shop == null) { // Allow the chest to be opened, it is just regular chest
                    event.isCancelled = false
                    return
                } else { // View the shop
                    if (shop.owner.isPlayer(event.player)) { // If the player is the owner
                        viewShopOwner(event, shop)
                    }
                    else { // It is a customer
                        viewShopCustomer(event, shop)
                    }
                }
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
        val data1: Pair<ShopType, ItemStack> = plugin.menus.shopCreateMenu.request()
    }

    fun viewShopCustomer(event: PlayerInteractEvent, shop: Shop) {

    }

    fun viewShopOwner(event: PlayerInteractEvent, shop: Shop) {

    }
}