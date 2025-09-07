package com.brinkmc.plop.shared.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.controller.gui.nexus.NexusGui
import com.brinkmc.plop.plot.controller.gui.nexus.PlotLogGui
import com.brinkmc.plop.plot.controller.gui.nexus.TotemGui
import com.brinkmc.plop.plot.controller.gui.nexus.UpgradeGui
import com.brinkmc.plop.plot.controller.gui.preview.PreviewHotbar
import com.brinkmc.plop.plot.controller.gui.selector.PlotTypeMenu
import com.brinkmc.plop.plot.controller.gui.selector.SelectionSelfMenu
import com.brinkmc.plop.plot.controller.gui.visit.MenuPlotList
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.Gui
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.gui.shop.access.MenuShopMain
import com.brinkmc.plop.shared.gui.shop.access.customer.MenuBuy
import com.brinkmc.plop.shared.gui.shop.access.customer.MenuSell
import com.brinkmc.plop.shared.gui.shop.access.owner.MenuShopLogs
import com.brinkmc.plop.shared.gui.shop.access.owner.MenuShopSettings
import com.brinkmc.plop.shared.gui.shop.init.MenuShopCreate
import com.brinkmc.plop.shared.gui.shop.init.item.MenuShopItem
import com.brinkmc.plop.shared.gui.shop.init.price.MenuShopBuy
import com.brinkmc.plop.shared.gui.shop.init.price.MenuShopBuyLimit
import com.brinkmc.plop.shared.gui.shop.init.price.MenuShopSell
import com.brinkmc.plop.shared.gui.shop.init.stock.MenuShopStock
import com.brinkmc.plop.shop.controller.gui.customer.BuyFromShopGui
import com.noxcrew.interfaces.view.InterfaceView
import org.bukkit.entity.Player

class MenuService(override val plugin: Plop): Addon, State {

    val buyFromShopGui = BuyFromShopGui(plugin)
    val previewHotbar = PreviewHotbar(plugin)

    override suspend fun load() {
        listOf(
            buyFromShopGui
        ).forEach {
            it = it::class.constructors.first().call(plugin)
        }
    }

    internal suspend fun open(player: Player, gui: Gui, view: InterfaceView? = null, vararg args: Any): InterfaceView {
        val inv = gui.inventory(player)
        return plugin.asyncScope {
            inventory.open(player, view)
        }
    }

    override suspend fun kill() {
        TODO("Not yet implemented")
    }

}