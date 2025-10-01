package com.brinkmc.plop.shop.controller.gui.customer

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.util.CoroutineUtils.async
import com.brinkmc.plop.shop.controller.gui.ShopGui
import com.noxcrew.interfaces.drawable.Drawable.Companion.drawable
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.interfaces.ChestInterface
import com.noxcrew.interfaces.interfaces.ChestInterfaceBuilder
import com.noxcrew.interfaces.interfaces.buildChestInterface
import com.noxcrew.interfaces.view.InterfaceView
import org.bukkit.entity.Player

class BuyFromShopGui(override val plugin: Plop): ShopGui {



    private suspend fun inventory(vararg args: Any): ChestInterface = buildChestInterface {

        setupBackButton()
        setupIncrementButtons()

        setupConfirmButton()
        return@buildChestInterface
    }


    private suspend fun ChestInterfaceBuilder.setupConfirmButton() {
        withTransform { pane, view ->
            val playerId = view.player.uniqueId
            val shopId = shopAccessService.getViewedShop(playerId) ?: return@withTransform

            pane[3,3] = StaticElement(
                drawable(
                    getConfirmButton()
                )
            ) { _ ->
                plugin.async {
                    val status = shopTransactionService.initialiseBuyTransaction(playerId, shopId)
                }
            }

        }
    }


    suspend fun open(player: Player, view: InterfaceView? = null, vararg args: Any): InterfaceView {
        return plugin.asyncScope {
            inventory(player, *args).open(player, view)
        }
    }

}