package com.brinkmc.plop.shared.gui.shop.owner

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.shared.base.Addon
import com.noxcrew.interfaces.drawable.Drawable.Companion.drawable
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.interfaces.buildChestInterface
import com.noxcrew.interfaces.properties.interfaceProperty
import com.noxcrew.interfaces.view.ChestInterfaceView
import com.noxcrew.interfaces.view.InterfaceView
import com.noxcrew.interfaces.view.PlayerInterfaceView
import org.bukkit.entity.Player

class MenuShopOwner(override val plugin: Plop): Addon {

    private val inventory = buildChestInterface {
        onlyCancelItemInteraction = false
        prioritiseBlockInteractions = false

        rows = 5

        val stockProperty = interfaceProperty(0)
        var stock by stockProperty

        withTransform(stockProperty) { pane, view ->
            stock = 0
        }

    }

    suspend fun open(player: Player, parent: InterfaceView? = null): ChestInterfaceView {
        return inventory.open(player, parent)
    }
}