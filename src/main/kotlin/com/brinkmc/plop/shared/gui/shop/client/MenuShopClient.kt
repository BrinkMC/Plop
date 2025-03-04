package com.brinkmc.plop.shared.gui.shop.client

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.noxcrew.interfaces.interfaces.buildChestInterface
import com.noxcrew.interfaces.view.ChestInterfaceView
import com.noxcrew.interfaces.view.InterfaceView
import org.bukkit.entity.Player

class MenuShopClient(override val plugin: Plop): Addon  {

    private val inventory = buildChestInterface {
        onlyCancelItemInteraction = false
        prioritiseBlockInteractions = false

        rows = 5

        withTransform { pane, view ->

        }

    }

    suspend fun open(player: Player, parent: InterfaceView? = null): ChestInterfaceView {
        return inventory.open(player, parent)
    }
}