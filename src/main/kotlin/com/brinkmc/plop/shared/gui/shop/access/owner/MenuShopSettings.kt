package com.brinkmc.plop.shared.gui.shop.access.owner

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shop.shop.Shop
import com.noxcrew.interfaces.element.Element.EMPTY.drawable
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.interfaces.ChestInterfaceBuilder
import com.noxcrew.interfaces.interfaces.buildChestInterface
import com.noxcrew.interfaces.view.InterfaceView
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class MenuShopSettings(override val plugin: Plop): Addon {


    object BaseItems {
        val HANDLE_BUY = ItemStack(Material.HOPPER)
        val HANDLE_SELL = ItemStack(Material.GOLD_INGOT)
        val CLICK_ENABLE = ItemStack(Material.GRAY_STAINED_GLASS_PANE)
        val STOCK = ItemStack(Material.BARREL)
        val TRANSACTION_LOG = ItemStack(Material.PAPER)
        val CLOSE_SHOP = ItemStack(Material.IRON_DOOR)
        val OPEN_SHOP = ItemStack(Material.OAK_DOOR)
        val DELETE_SHOP = ItemStack(Material.BARRIER)
        val BACK = ItemStack(Material.REDSTONE)
        val BAD = ItemStack(Material.GRAY_STAINED_GLASS_PANE)
    }

    private fun inventory(player: Player, shop: Shop) = buildChestInterface {
        // Internal settings menu for shop, only to owners
        onlyCancelItemInteraction = false
        prioritiseBlockInteractions = false
        rows = 5

        // Shop stock button
        shopStock(shop)
    }

    private fun ChestInterfaceBuilder.shopStock(shop: Shop) {
        // Shop stock
        withTransform { pane, view ->
            pane[2, 0] = StaticElement(drawable(

            )) { (player) ->
                plugin.async {
                    plugin.menus.shopInitStockMenu
                }
            }
        }
    }

    suspend fun open(player: Player, shop: Shop, parent: InterfaceView? = null) {
        inventory(player, shop).open(player, parent)
    }

}