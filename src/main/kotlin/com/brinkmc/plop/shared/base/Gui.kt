package com.brinkmc.plop.shared.base

import com.brinkmc.plop.shared.constant.ItemKey
import com.brinkmc.plop.shared.constant.MessageKey
import com.brinkmc.plop.shared.util.CoroutineUtils.async
import com.noxcrew.interfaces.drawable.Drawable.Companion.drawable
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.interfaces.ChestInterfaceBuilder
import com.noxcrew.interfaces.view.InterfaceView
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.UUID

internal interface Gui: Addon {

    private suspend fun getBackButton(): ItemStack {
        return messages.getItem(
            ItemKey.BACK,
            MessageKey.MENU_BACK_NAME,
            MessageKey.MENU_BACK_DESC,
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

    suspend fun open(playerId: UUID, view: InterfaceView?, vararg args: Any): Any?

    suspend fun close(playerId: UUID) {
        playerService.closeMenu(playerId)
    }


}