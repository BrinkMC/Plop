package com.brinkmc.plop.shared.hooks.listener

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.util.PersistentDataInterpreter
import com.brinkmc.plop.shared.util.description
import com.brinkmc.plop.shared.util.name
import com.google.gson.Gson
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Item
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.persistence.PersistentDataType

class InventoryClick(override val plugin: Plop): Addon, State, Listener {
    override fun load() {}

    override fun kill() {}

    /*
    Persistent data key will be plop so that I can identify if the item is mine
    Inside the item will be a string form of a data class
    The data class will contain the UUID of the player the button belongs to (menu wise)
    The data will also contain the exact button it is
     */
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
        val item = inventoryClickEvent.currentItem
        val container = item?.itemMeta?.persistentDataContainer

        // Try and get plop data
        val rawData = container?.get(plugin.namespacedKey, PersistentDataType.STRING)
        val readData: PersistentDataInterpreter = Gson().fromJson(rawData, PersistentDataInterpreter::class.java)

        when (readData.buttonName) { // Check via PDC for what button it is
            "BACK_BUTTON" -> {
                //TODO Fix up PDI system! Arguably make it a separate system and then hook in when needed
            }
            "FORWARD_BUTTON" -> {

            }
        }
    }
}