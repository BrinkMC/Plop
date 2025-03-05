package com.brinkmc.plop.shared.gui.shop.owner

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.PlotType
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
import com.noxcrew.interfaces.view.PlayerInterfaceView
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Chest
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class MenuShopOwner(override val plugin: Plop): Addon {

    val temporaryShop = mutableMapOf<Player, Location>()

    val TRANSACTION_LOG: ItemStack
        get() = ItemStack(Material.PAPER)
            .name("shop.log.name")
            .description("Shop.log.desc")

    val WARES: ItemStack
        get() = ItemStack(Material.BARREL)
            .name("shop.wares.name")
            .description("shop.wares.desc")
    // Delete shop
    val DELETE_SHOP: ItemStack
        get() = ItemStack(Material.REDSTONE_BLOCK)
            .name("shop.delete.name")
            .description("shop.delete.desc")

    // Toggle items
    val CLOSE_SHOP: ItemStack
        get() = ItemStack(Material.DEAD_BUBBLE_CORAL_FAN)
            .name("shop.close.name")
            .description("shop.close.desc")

    val OPEN_SHOP: ItemStack
        get() = ItemStack(Material.BUBBLE_CORAL_FAN)
            .name("shop.open.name")
            .description("shop.open.desc")

    val BUY_SHOP: ItemStack
        get() = ItemStack(Material.GOLD_INGOT)
            .name("shop.buy.name")
            .description("shop.buy.desc")

    val SELL_SHOP: ItemStack
        get() = ItemStack(Material.NETHER_WART)
            .name("shop.sell.name")
            .description("shop.sell.desc")

    val PLEASE: ItemStack
        get() = ItemStack(Material.BARRIER)


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

            pane[3, 3] = StaticElement(drawable(TRANSACTION_LOG)) { (player) -> plugin.async {
                plugin.menus.shopLogsMenu.open(player, view)
            } }

            pane[3, 5] = StaticElement(drawable(WARES)) { (player) -> plugin.async {
                plugin.menus.shopWareOwnerMenu.open(player, view)
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
                pane[5,0] = StaticElement(drawable(
                    PLEASE
                        .name("shop.close.bad.name")
                        .description("shop.close.bad.desc")
                ))

                pane[5,5] = StaticElement(drawable(
                    PLEASE
                        .name("shop.toggle.bad.name")
                        .description("shop.toggle.bad.desc")
                ))
            }
        }
    }

    suspend fun open(player: Player, parent: InterfaceView? = null): ChestInterfaceView {
        return inventory.open(player, parent)
    }
}