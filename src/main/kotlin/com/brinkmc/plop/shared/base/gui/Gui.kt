package com.brinkmc.plop.shared.base.gui

import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.constant.ItemKey
import com.brinkmc.plop.shared.constant.MessageKey
import com.brinkmc.plop.shared.util.CoroutineUtils.async
import com.noxcrew.interfaces.drawable.Drawable
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.interfaces.ContainerInterfaceBuilder
import com.noxcrew.interfaces.view.InterfaceView
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

    fun ContainerInterfaceBuilder.Simple.setupBackButton(row: Int, col: Int) {
        withTransform { pane, view ->
            pane[row, col] = StaticElement(
                Drawable.drawable(
                    getBackButton()
                )
            ) { _ ->
                plugin.async {
                    view.close()
                }
            }
        }
    }

    suspend fun open(playerId: UUID, view: InterfaceView?, vararg args: Any): InterfaceView?

    suspend fun close(playerId: UUID) {
        playerService.closeMenu(playerId)
    }


}