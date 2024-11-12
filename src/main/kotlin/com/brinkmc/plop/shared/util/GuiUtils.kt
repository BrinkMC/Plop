package com.brinkmc.plop.shared.util

import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack

/*
Credit to NoxCrew in the interfaces-kotlin library for these handy extensions
 */

public fun ItemStack.name(name: String): ItemStack {
    itemMeta = itemMeta.also { meta ->
        meta.displayName(Component.text(name))
    }
    return this
}

public fun ItemStack.description(description: String): ItemStack {
    itemMeta = itemMeta.also { meta ->
        meta.lore(listOf(Component.text(description)))
    }
    return this
}