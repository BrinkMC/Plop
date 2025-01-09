package com.brinkmc.plop.shared.gui.preview

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.util.description
import com.brinkmc.plop.shared.util.name
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class HotbarPreview(override val plugin: Plop): Addon {

    val BACK_BUTTON: ItemStack = ItemStack(Material.ARROW)
        .name(lang.getC("preview.back-button.name"))
        .description(lang.getC("preview.back-button.desc"))

    val FORWARD_BUTTON: ItemStack = ItemStack(Material.ARROW)
        .name(lang.getC("preview.forward-button.name"))
        .description(lang.getC("preview.forward-button.desc"))



}