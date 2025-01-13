package com.brinkmc.plop.shared.gui.preview

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.util.description
import com.brinkmc.plop.shared.util.name
import com.brinkmc.plop.shared.util.setPData
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

class HotbarPreview(override val plugin: Plop, val player: UUID): Addon {

    val BACK_BUTTON: ItemStack = ItemStack(Material.ARROW)
        .name(lang.getC("preview.back-button.name"))
        .description(lang.getC("preview.back-button.desc"))
        .setPData(plugin, player, ::BACK_BUTTON)

    val FORWARD_BUTTON: ItemStack = ItemStack(Material.ARROW)
        .name(lang.getC("preview.forward-button.name"))
        .description(lang.getC("preview.forward-button.desc"))
        .setPData(plugin, player, ::FORWARD_BUTTON)



}