package com.brinkmc.plop.shared.hotbar.preview

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.pdc.storeData
import com.brinkmc.plop.shared.pdc.types.PDButtonInstance
import com.brinkmc.plop.shared.util.description
import com.brinkmc.plop.shared.util.name
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.UUID

class HotbarPreview(override val plugin: Plop, val player: UUID): Addon {

    // Constants, save player as well so it is identifiable
    val BACK_BUTTON: ItemStack = ItemStack(Material.ARROW)
        .name(lang.get("preview.back-button.name"))
        .description(lang.get("preview.back-button.desc"))
        .storeData(PDButtonInstance(player, "BACK_BUTTON"))

    val FORWARD_BUTTON: ItemStack = ItemStack(Material.ARROW)
        .name(lang.get("preview.forward-button.name"))
        .description(lang.get("preview.forward-button.desc"))
        .storeData(PDButtonInstance(player, "FORWARD_BUTTON"))

    val CONFIRM_BUTTON: ItemStack = ItemStack(Material.ARROW)
        .name(lang.get("preview.confirm-button.name"))
        .description(lang.get("preview.confirm-button.desc"))
        .storeData(PDButtonInstance(player, "CONFIRM_BUTTON"))

    val TOGGLE_BUTTON: ItemStack = ItemStack(Material.ARROW)
        .name(lang.get("preview.toggle-button.name"))
        .description(lang.get("preview.toggle-button.desc"))
        .storeData(PDButtonInstance(player, "TOGGLE_BUTTON"))

}