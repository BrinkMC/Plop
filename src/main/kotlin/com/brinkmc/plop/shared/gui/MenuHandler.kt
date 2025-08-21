package com.brinkmc.plop.shared.gui

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.gui.nexus.MenuNexusMain
import com.brinkmc.plop.shared.gui.nexus.MenuPlotLogs
import com.brinkmc.plop.shared.gui.nexus.MenuTotemList
import com.brinkmc.plop.shared.gui.nexus.MenuUpgrade
import com.brinkmc.plop.shared.gui.preview.HotbarPreview
import com.brinkmc.plop.shared.gui.selector.SelectionOtherMenu
import com.brinkmc.plop.shared.gui.selector.SelectionSelfMenu
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
import com.brinkmc.plop.shared.gui.visit.MenuPlotList

class MenuHandler(val plugin: Plop) {
    // Selectors
    val selectionSelfMenu = SelectionSelfMenu(plugin)
    val selectionOtherMenu = SelectionOtherMenu(plugin)

    // Hotbar
    val hotbarPreview = HotbarPreview(plugin)

    // Nexus configs
    val nexusMainMenu = MenuNexusMain(plugin)
    val nexusLogsMenu = MenuPlotLogs(plugin)
    val nexusTotemsMenu = MenuTotemList(plugin)
    val nexusUpgradeMenu = MenuUpgrade(plugin)

    // Plot Visit
    val plotVisitMenu = MenuPlotList(plugin)

    // Shop menus creation
    val shopInitCreateMenu = MenuShopCreate(plugin)
    val shopInitItemMenu = MenuShopItem(plugin)
    val shopInitBuyMenu = MenuShopBuy(plugin)
    val shopInitBuyLimitMenu = MenuShopBuyLimit(plugin)
    val shopInitSellMenu = MenuShopSell(plugin)
    val shopInitStockMenu = MenuShopStock(plugin)

    val shopMainMenu = MenuShopMain(plugin)

    // Shop owner menus
    val shopLogsMenu = MenuShopLogs(plugin)
    val shopSettingsMenu = MenuShopSettings(plugin)

    // Shop client menus
    val shopBuyMenu = MenuBuy(plugin)
    val shopSellMenu = MenuSell(plugin)

}