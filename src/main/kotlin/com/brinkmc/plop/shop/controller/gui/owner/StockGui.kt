package com.brinkmc.plop.shop.controller.gui.owner

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shop.dto.Shop
import com.noxcrew.interfaces.interfaces.buildCombinedInterface
import com.noxcrew.interfaces.view.InterfaceView
import org.bukkit.entity.Player

class StockGui(override val plugin: Plop): Addon {

    private fun inventory(player: Player, shop: Shop) = buildCombinedInterface {
        onlyCancelItemInteraction = false
        prioritiseBlockInteractions = false
        rows = 5

        setupBackButton(shop)
        setupShopStock(shop)
        setupPlayerInventory(shop)
    }

    suspend fun open(player: Player, parentView: InterfaceView? = null): InterfaceView {
        shopStockService.storeInventory(player)

    }
}