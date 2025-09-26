package com.brinkmc.plop.shared.item

import com.brinkmc.plop.Plop
import com.brinkmc.plop.factory.constant.FactoryType
import com.brinkmc.plop.plot.constant.PlotItems
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.constant.ItemKey
import com.brinkmc.plop.shared.constant.MessageKey
import com.brinkmc.plop.shared.item.enum.TrackedItemKey
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class ItemService(override val plugin: Plop): Addon, State {

    private val key = NamespacedKey(plugin, "plop_tracked_item")

    suspend fun createTrackedItem(trackedItemKey: TrackedItemKey): ItemStack {

        val item: ItemStack = when (trackedItemKey) {
            FactoryType.PLANTER -> {
                messages.getItem(FactoryType.PLANTER.item, MessageKey.FACTORY_PLANTER_NAME, MessageKey.FACTORY_PLANTER_DESC)
            }

            FactoryType.HARVESTER -> {
                messages.getItem(FactoryType.HARVESTER.item, MessageKey.FACTORY_HARVESTER_NAME, MessageKey.FACTORY_HARVESTER_DESC)
            }

            FactoryType.PLACER -> {
                messages.getItem(FactoryType.PLACER.item, MessageKey.FACTORY_PLACER_NAME, MessageKey.FACTORY_PLACER_DESC)
            }

            FactoryType.BREAKER -> {
                messages.getItem(FactoryType.BREAKER.item, MessageKey.FACTORY_BREAKER_NAME, MessageKey.FACTORY_BREAKER_DESC)
            }

            PlotItems.NEXUS_BOOK -> {
                messages.getItem(PlotItems.NEXUS_BOOK.item, MessageKey.NEXUS_BOOK_NAME, MessageKey.NEXUS_BOOK_DESC)
            }

            else -> {
                messages.getItem(ItemKey.UNKNOWN_ITEM, MessageKey.UNKNOWN_ITEM_NAME, MessageKey.UNKNOWN_ITEM_DESC)
            }
        }

        // Store it in item
        item.editPersistentDataContainer { pdc ->
            pdc.set(key, PersistentDataType.STRING, trackedItemKey.toString())
        }

        return item
    }

    fun getTrackingType(item: ItemStack): TrackedItemKey? {
        val pdc = item.persistentDataContainer
        val value = pdc.get(key, PersistentDataType.STRING) ?: return null
        // Value of won't work because hte key itself is an interface which is inherited by multiple enums
        // Try FactoryType
        FactoryType.entries.find { it.name == value }?.let { return it }
        // Try PlotItems
        PlotItems.entries.find { it.name == value }?.let { return it }

        return null
    }

    override suspend fun load() {
        TODO("Not yet implemented")
    }

    override suspend fun kill() {
        TODO("Not yet implemented")
    }

}