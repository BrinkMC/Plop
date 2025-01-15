package com.brinkmc.plop.shared.hooks.listener

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.preview.PreviewInstance
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.pdc.PersistentData
import com.brinkmc.plop.shared.pdc.retrieveData
import com.brinkmc.plop.shared.pdc.types.PDButtonInstance
import com.brinkmc.plop.shared.util.sync
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.PlayerInventory
import org.bukkit.persistence.PersistentDataType

class InventoryClick(override val plugin: Plop): Addon, State, Listener {
    override suspend fun load() {}

    override suspend fun kill() {}

    /*
    Persistent data key will be plop so that I can identify if the item is mine
    Inside the item will be a string form of a data class
    The data class will contain the UUID of the player the button belongs to (menu wise)
    The data will also contain the exact button it is
     */
    @EventHandler
    suspend fun onInventoryClick(inventoryClickEvent: InventoryClickEvent) {
        preventChangeDuringPreview(inventoryClickEvent) // Check preview
    }

    private fun preventChangeDuringPreview(inventoryClickEvent: InventoryClickEvent) {

        if (inventoryClickEvent.clickedInventory !is PlayerInventory) { // Check if it is player inventory
            return
        }

        val potentialPreview = plots.plotPreviewHandler.previews[inventoryClickEvent.whoClicked.uniqueId] ?: return // Check is in preview

        if (inventoryClickEvent.click.isRightClick) { // Allow right_clicks
            previewItem(inventoryClickEvent, potentialPreview)
            return
        }

        inventoryClickEvent.isCancelled = true // Prevent anything else
    }

    private fun previewItem(inventoryClickEvent: InventoryClickEvent, preview: PreviewInstance) {
        val item = inventoryClickEvent.currentItem

        var data = item?.retrieveData(PDButtonInstance::class.java) as PDButtonInstance
        // Try and get plop data


        val rawData = container?.get(plugin.namespacedKey, PersistentDataType.STRING)
        val readData: PersistentData = Gson().fromJson(rawData, PersistentData::class.java)

        when (readData.buttonName) { // Check via PDC for what button it is
            "BACK_BUTTON" -> {
                //TODO Fix up PDI system! Arguably make it a separate system and then hook in when needed
            }
            "FORWARD_BUTTON" -> {

            }
        }
    }
}