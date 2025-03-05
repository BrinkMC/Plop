package com.brinkmc.plop.shared.gui.shop.access.owner

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shop.shop.Shop
import com.brinkmc.plop.shop.shop.ShopType
import com.noxcrew.interfaces.drawable.Drawable.Companion.drawable
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.interfaces.buildChestInterface
import com.noxcrew.interfaces.properties.InterfaceProperty
import com.noxcrew.interfaces.properties.interfaceProperty
import com.noxcrew.interfaces.view.ChestInterfaceView
import com.noxcrew.interfaces.view.InterfaceView
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Chest
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class MenuShopLogs(override val plugin: Plop): Addon {

    val LOG: ItemStack
        get() = ItemStack(Material.PAPER)

    val BACK: ItemStack
        get() = ItemStack(Material.REDSTONE)
            .name("menu.back")

    private fun inventory(player: Player, shop: Shop) = buildChestInterface {
        onlyCancelItemInteraction = false
        prioritiseBlockInteractions = false

        rows = 5

        withTransform { pane, view ->
            val shopLogs = shop.transactions.reversed() // Get the order of transactions latest first
            // Draw every slot in the "chest" with items till full
            for (log in shopLogs) {
                for (i in 0..3) {
                    for (j in 0..8) {
                        val placeholders = arrayOf(
                            // Calculate time since log.timestamp
                            Placeholder.component("date", Component.text(System.currentTimeMillis() - log.timestamp.time ) ), // Current date - log.timestamp
                            Placeholder.component("shop.log.amount", log.amount.toString()),
                            Placeholder.component("shop.log.price", log.price.toString())
                        )

                        pane[i, j] = StaticElement(drawable(
                            LOG.name("shop.log.name")
                                .description("shop.log.desc",)
                        )) { (player) -> plugin.async {
                            // Do something with the log
                        }
                        ))
                    }
                }
            }
        }
    }

    suspend fun open(player: Player, shop: Shop, parent: InterfaceView? = null): ChestInterfaceView {
        return inventory(player, shop).open(player, parent)
    }

}