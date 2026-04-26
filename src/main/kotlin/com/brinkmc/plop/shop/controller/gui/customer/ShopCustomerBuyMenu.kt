package com.brinkmc.plop.shop.controller.gui.customer

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.gui.Gui
import com.brinkmc.plop.shared.constant.ItemKey
import com.brinkmc.plop.shared.constant.MessageKey
import com.noxcrew.interfaces.interfaces.ContainerInterface
import com.noxcrew.interfaces.interfaces.ContainerInterfaceBuilder
import com.noxcrew.interfaces.interfaces.buildChestInterface
import org.bukkit.inventory.ItemStack
import java.util.UUID

class ShopCustomerBuyMenu(override val plugin: Plop): Gui {

    private suspend fun getShopItem(playerId: UUID? = null, shopId: UUID): ItemStack {
        return messages.getItem(
            ItemKey.SHOP_ITEM,
            MessageKey.MENU_SHOP_ITEM_NAME,
            MessageKey.MENU_SHOP_ITEM_DESC,
            playerId = playerId,
            shopId = shopId
        )
    }

    private suspend fun getIncreaseQuantityItem(playerId: UUID? = null, shopId: UUID): ItemStack {
        return messages.getItem(
            ItemKey.INCREASE_QUANTITY,
            MessageKey.MENU_INCREASE_QUANTITY_NAME,
            MessageKey.MENU_INCREASE_QUANTITY_DESC,
            playerId = playerId,
            shopId = shopId
        )
    }

    private suspend fun getDecreaseQuantityItem(playerId: UUID? = null, shopId: UUID): ItemStack {
        return messages.getItem(
            ItemKey.DECREASE_QUANTITY,
            MessageKey.MENU_DECREASE_QUANTITY_NAME,
            MessageKey.MENU_DECREASE_QUANTITY_DESC,
            playerId = playerId,
            shopId = shopId
        )
    }



    private suspend fun inventory(vararg args: Any): ContainerInterface.Simple = buildChestInterface {

        val playerId = args[0] as UUID
        val shopId = args[1] as UUID

        setupShopItem(playerId, shopId)
        setupIncreaseQuantityItem(playerId,  shopId)
        setupDecreaseQuantityItem(playerId, shopId)

        return@buildChestInterface
    }

    fun ContainerInterfaceBuilder.Simple.setupShopItem(playerId: UUID?, shopId: UUID) {

    }

    fun ContainerInterfaceBuilder.Simple.setupIncreaseQuantityItem(playerId: UUID?, shopId: UUID) {

    }

    fun ContainerInterfaceBuilder.Simple.setupDecreaseQuantityItem(playerId: UUID?, shopId: UUID) {
        shopQuantityService.getOpenAccess()

    }

}