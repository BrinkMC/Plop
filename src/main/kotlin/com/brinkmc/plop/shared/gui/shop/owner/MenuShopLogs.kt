package com.brinkmc.plop.shared.gui.shop.owner

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
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Chest
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class MenuShopLogs(override val plugin: Plop): Addon {

    private val temporaryShop: MutableMap<Player, Location>
        get() = plugin.menus.shopOwnerMenu.temporaryShop

    val LOG: ItemStack
        get() = ItemStack(Material.PAPER)

    val BACK: ItemStack
        get() = ItemStack(Material.REDSTONE)
            .name("menu.back")

    private val inventory = buildChestInterface {
        onlyCancelItemInteraction = false
        prioritiseBlockInteractions = false

        rows = 6

        val shopProperty: InterfaceProperty<Shop?> = interfaceProperty(null)
        var shop by shopProperty

        withTransform(shopProperty) { pane, view ->
            val temp = temporaryShop[view.player] ?: return@withTransform
            val chest = temp as? Chest ?: return@withTransform
            shop = chest.toShop() ?: return@withTransform

            if (shop == null) {
                return@withTransform
            }

            // Totem list populate for that
            var k = 0
            for (i in 0..8) {
                for (j in 0..5) {
                    while (k < shop. .size) {
                        val totem = totemItems[k] // Populate every item of pane with totem till no more
                        k++
                        pane[i, j] = StaticElement(
                            drawable(
                                totem
                            )
                        ) // Potential for a click event in future if required
                    }
                }
            }

            // Back button
            pane[5, 8] = StaticElement(drawable(BACK)) { (player) -> plugin.async {
                view.back()
            } }

        }

        withTransform(shopProperty) { pane, view ->
            val temp = temporaryShop[view.player] ?: return@withTransform
            val chest = temp as? Chest ?: return@withTransform
            shop = chest.toShop() ?: return@withTransform

            if (shop == null) {
                return@withTransform
            }

            pane[5, 3] = if (shop.open) {
                StaticElement(drawable(OPEN_SHOP)) { (player) -> plugin.async {
                    //TODO open logic
                } }
            } else {
                StaticElement(drawable(CLOSE_SHOP)) { (player) -> plugin.async {
                    //TODO close logic
                } }
            }

            // Show all stock zero actions
            if (shop.stock == 0) {
                pane[5,0] = StaticElement(drawable(DELETE_SHOP)) { (player) -> plugin.async {
                    //TODO delete logic
                } }

                pane[5,5] = if(shop.shopType == ShopType.BUY) {
                    StaticElement(drawable(BUY_SHOP)) { (player) -> plugin.async {
                        //TODO buy logic
                    } }
                } else {
                    StaticElement(drawable(SELL_SHOP)) { (player) -> plugin.async {
                        //TODO sell logic
                    } }
                }
            } else { // They shouldn't be allowed to do it
                pane[5,0] = StaticElement(
                    drawable(
                    PLEASE
                        .name("shop.close.bad.name")
                        .description("shop.close.bad.desc")
                )
                )

                pane[5,5] = StaticElement(
                    drawable(
                    PLEASE
                        .name("shop.toggle.bad.name")
                        .description("shop.toggle.bad.desc")
                )
                )
            }
        }
    }

    suspend fun open(player: Player, parent: InterfaceView? = null): ChestInterfaceView {
        return inventory.open(player, parent)
    }

}