package com.brinkmc.plop.plot.constant

import com.brinkmc.plop.shared.item.enum.TrackedItemKey
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

enum class PlotItems(val item: ItemStack) : TrackedItemKey {
    NEXUS_BOOK(ItemStack(Material.WRITTEN_BOOK)),
    TOTEM(ItemStack(Material.WRITTEN_BOOK))
}