package com.brinkmc.plop.shared.gui.shop.access.owner

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shop.shop.Shop
import com.noxcrew.interfaces.interfaces.buildChestInterface
import com.noxcrew.interfaces.view.InterfaceView
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class MenuShopSettings(override val plugin: Plop): Addon {

    val HANDLE_BUY: ItemStack
        get() = ItemStack(Material.HOPPER)

    val HANDLE_SELL: ItemStack
        get() = ItemStack(Material.GOLD_INGOT)

    val CLICK_ENABLE: ItemStack
        get() = ItemStack(Material.GRAY_STAINED_GLASS_PANE)

    val STOCK: ItemStack
        get() = ItemStack(Material.BARREL)

    val TRANSACTION_LOG: ItemStack
        get() = ItemStack(Material.PAPER)

    val CLOSE_SHOP: ItemStack
        get() = ItemStack(Material.IRON_DOOR)

    val OPEN_SHOP
        get() = ItemStack(Material.OAK_DOOR)

    val DELETE_SHOP
        get() = ItemStack(Material.BARRIER)

    val BACK: ItemStack
        get() = ItemStack(Material.REDSTONE)

    val BAD: ItemStack
        get() = ItemStack(Material.GRAY_STAINED_GLASS_PANE)

    private fun inventory(player: Player, shop: Shop) = buildChestInterface {
        // Internal settings menu for shop, only to owners
        onlyCancelItemInteraction = false
        prioritiseBlockInteractions = false
        rows = 5



        withTransform()
    }

    suspend fun open(player: Player, shop: Shop, parent: InterfaceView? = null) {
        inventory(player, shop).open(player, parent)
    }

}