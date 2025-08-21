package com.brinkmc.plop.shared.hook.listener

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class GeneralListener(override val plugin: Plop): Addon, State, Listener {
    override suspend fun load() {}

    override suspend fun kill() {}

    @EventHandler(priority = org.bukkit.event.EventPriority.MONITOR)
    suspend fun onPlayerJoin(playerJoinEvent: PlayerJoinEvent) {
        playerJoinEvent.player.personalPlot() // Populate the cache when they join so that first command isn't so slow
        playerJoinEvent.player.guildPlot()
        playerJoinEvent.player.updateBorder()
    }

    @EventHandler
    suspend fun onStock(event: PlayerQuitEvent) {
        val player = event.player

        if (plugin.menus.shopInitStockMenu.inventoryClone[player] == null) {
            return
        }

        // They are in the shop ware menu, save their inventory
        plugin.menus.shopInitStockMenu.returnInventory(player)
        // Remove from list
        plugin.menus.shopInitStockMenu.inventoryClone.remove(player)
    }

    @EventHandler
    suspend fun onItem(event: PlayerQuitEvent) {
        val player = event.player

        if (plugin.menus.shopInitItemMenu.inventoryClone[player] == null) {
            return
        }

        // They are in the shop ware menu, save their inventory
        plugin.menus.shopInitItemMenu.returnInventory(player)
        // Remove from list
        plugin.menus.shopInitItemMenu.inventoryClone.remove(player)
    }

}