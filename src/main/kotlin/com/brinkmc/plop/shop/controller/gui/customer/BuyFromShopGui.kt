package com.brinkmc.plop.shop.controller.gui.customer

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.design.enums.ItemKey
import com.brinkmc.plop.shared.design.enums.MessageKey
import com.brinkmc.plop.shared.util.CoroutineUtils.async
import com.brinkmc.plop.shop.controller.gui.ShopGui
import com.brinkmc.plop.shop.dto.Shop
import com.noxcrew.interfaces.drawable.Drawable.Companion.drawable
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.interfaces.ChestInterface
import com.noxcrew.interfaces.interfaces.ChestInterfaceBuilder
import com.noxcrew.interfaces.interfaces.buildChestInterface
import com.noxcrew.interfaces.view.InterfaceView
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class BuyFromShopGui(override val plugin: Plop): ShopGui {



    override suspend fun inventory(vararg args: Any): ChestInterface = buildChestInterface {
        val player = args[0] as Player

        val shopId = shopAccessService.getViewedShop(player.uniqueId) ?: return@buildChestInterface

        setupBackButton()
        setupIncrementButtons()

        setupConfirmButton()
        return@buildChestInterface
    }

    suspend fun ChestInterfaceBuilder.setupConfirmButton() {
        withTransform { pane, view ->
            val playerId = view.player.uniqueId
            val shopId = shopAccessService.getViewedShop(playerId) ?: return@withTransform

            pane[3,3] = StaticElement(
                drawable(
                    getConfirmButton()
                )
            ) { _ ->
                shopTransactionService.checkTransaction(playerId, shopId, shopAccessService.getTotal(playerId), )
            }

        }
    }


}