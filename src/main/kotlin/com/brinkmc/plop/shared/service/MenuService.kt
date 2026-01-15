package com.brinkmc.plop.shared.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.controller.gui.nexus.NexusMenu
import com.brinkmc.plop.plot.controller.gui.nexus.PlotLogMenu
import com.brinkmc.plop.plot.controller.gui.nexus.TotemMenu
import com.brinkmc.plop.plot.controller.gui.nexus.UpgradeMenu
import com.brinkmc.plop.plot.controller.gui.preview.PreviewHotbar
import com.brinkmc.plop.plot.controller.gui.selector.PlotTypeMenu
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.constant.ServiceResult
import java.util.UUID

class MenuService(override val plugin: Plop): Addon, State {

    // Shop

    val buyFromShopMenu = BuyFromShopMenu(plugin)

    // Preview

    val previewHotbar = PreviewHotbar(plugin)

    // Nexus
    val nexusMenu = NexusMenu(plugin)
    val plotLogMenu = PlotLogMenu(plugin)
    val totemMenu = TotemMenu(plugin)
    val upgradeMenu = UpgradeMenu(plugin)

    // Selection Menus
    val plotTypeMenu = PlotTypeMenu(plugin)

    override suspend fun load() {

    }

    override suspend fun kill() {
        TODO("Not yet implemented")
    }
}