package com.brinkmc.plop.shared.hooks.listener

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.util.description
import com.brinkmc.plop.shared.util.name
import org.bukkit.Material
import org.bukkit.entity.Item
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory

class InventoryClick(override val plugin: Plop): Addon, State, Listener {
    override fun load() {}

    override fun kill() {}

    @EventHandler
    fun onInventoryClick(inventoryClickEvent: InventoryClickEvent) {
        preventChangeDuringPreview(inventoryClickEvent) // Check preview
    }

    private fun preventChangeDuringPreview(inventoryClickEvent: InventoryClickEvent) {

        if (inventoryClickEvent.clickedInventory !is PlayerInventory) { // Check if it is player inventory
            return
        }

        val potentialPreview = plots.plotPreviewHandler.previews.find { it.player == inventoryClickEvent.whoClicked.uniqueId }

        if (potentialPreview == null) { // Check is in preview
            return
        }

        if (inventoryClickEvent.click.isRightClick) { // Allow right_clicks
            previewItem(inventoryClickEvent)
            return
        }

        inventoryClickEvent.isCancelled = true // Prevent anything else
    }

    private fun previewItem(inventoryClickEvent: InventoryClickEvent) {
        if ()
    }
}