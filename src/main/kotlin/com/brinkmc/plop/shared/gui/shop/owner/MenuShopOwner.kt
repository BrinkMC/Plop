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
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Chest
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class MenuShopOwner(override val plugin: Plop): Addon {

    private val temporaryShop = mutableMapOf<Player, Location>()

    val TRANSACTION_LOG: ItemStack
        get() = ItemStack(Material.PAPER)
            .name("shop.log.name")
            .description("Shop.log.desc")



    private val inventory = buildChestInterface {
        onlyCancelItemInteraction = false
        prioritiseBlockInteractions = false

        rows = 6

        val stockProperty = interfaceProperty(0)
        var stock by stockProperty

        withTransform(stockProperty) { pane, view ->
            val temp = temporaryShop[view.player] ?: return@withTransform
            val chest = temp as? Chest ?: return@withTransform
            val shop = chest.toShop() ?: return@withTransform

            pane[3, 3] = StaticElement(drawable())

            pane[3, 5]
        }

    }

    suspend fun open(player: Player, parent: InterfaceView? = null): ChestInterfaceView {
        return inventory.open(player, parent)
    }
}