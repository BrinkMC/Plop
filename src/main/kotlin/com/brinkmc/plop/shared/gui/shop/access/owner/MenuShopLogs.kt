package com.brinkmc.plop.shared.gui.shop.access.owner

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shop.shop.Shop
import com.brinkmc.plop.shop.shop.ShopType
import com.noxcrew.interfaces.drawable.Drawable.Companion.drawable
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.interfaces.ChestInterface
import com.noxcrew.interfaces.interfaces.buildChestInterface
import com.noxcrew.interfaces.properties.InterfaceProperty
import com.noxcrew.interfaces.properties.interfaceProperty
import com.noxcrew.interfaces.view.ChestInterfaceView
import com.noxcrew.interfaces.view.InterfaceView
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Chest
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class MenuShopLogs(override val plugin: Plop): Addon {


    object BaseItems {
        val LOG = ItemStack(Material.PAPER)
        val BACK = ItemStack(Material.REDSTONE)
    }

    private fun inventory(player: Player, inputShop: Shop): ChestInterface = buildChestInterface {
        onlyCancelItemInteraction = false
        prioritiseBlockInteractions = false

        rows = 5

        withTransform { pane, view ->
            val shopLogs = inputShop.transactions.reversed() // Get the order of transactions latest first
            // Draw every slot in the "chest" with items till full
            for (log in shopLogs) {
                for (i in 0..3) {
                    for (j in 0..8) {
                        val placeholders = arrayOf(
                            // Calculate time since log.timestamp
                            Placeholder.component("date", Component.text(System.currentTimeMillis() - log.timestamp.time ) ), // Current date - log.timestamp
                            Placeholder.component("amount", Component.text(log.amount.toString()))
                        )

                        pane[i, j] = StaticElement(drawable(
                            BaseItems.LOG.get("shop.log.name", "shop.log.desc", args = placeholders)
                        ))
                    }
                }
            }
        }

        withTransform { pane, view ->
            pane[4, 4] = StaticElement(drawable(
                BaseItems.BACK.get("shop.back.name", "shop.back.desc")
            )) { (player) ->
                plugin.async {
                    view.close()
                }
            }
        }

        addCloseHandler { _, handler ->
            if (handler.parent() != null) {
                handler.parent()?.open()
                handler.parent()?.redrawComplete()
            }
        }
    }

    suspend fun open(player: Player, inputShop: Shop, parent: InterfaceView? = null): ChestInterfaceView {
        val inv = inventory(player = player, inputShop = inputShop)
        return inv.open(player, parent)
    }

}