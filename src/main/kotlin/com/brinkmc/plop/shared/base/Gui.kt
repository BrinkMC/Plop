package com.brinkmc.plop.shared.base

import com.brinkmc.plop.shared.design.enums.ItemKey
import com.brinkmc.plop.shared.design.enums.MessageKey
import com.brinkmc.plop.shared.util.CoroutineUtils.async
import com.noxcrew.interfaces.drawable.Drawable.Companion.drawable
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.interfaces.ChestInterfaceBuilder
import org.bukkit.inventory.ItemStack

internal interface Gui: Addon {

    suspend fun getBackButton(): ItemStack {
        return messages.getItem(
            ItemKey.BACK,
            MessageKey.MENU_BACK_BUTTON_NAME,
            MessageKey.MENU_BACK_BUTTON_DESC,
        )
    }

    fun ChestInterfaceBuilder.setupBackButton() {
        withTransform { pane, view ->
            pane[4, 4] = StaticElement(
                drawable(
                    getBackButton()
                )
            ) { _ ->
                plugin.async {
                    view.close()
                }
            }
        }
    }
}