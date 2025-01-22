package com.brinkmc.plop.plot

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.handler.PlotClaimHandler
import com.brinkmc.plop.plot.handler.PlotFactoryHandler
import com.brinkmc.plop.plot.handler.PlotHandler
import com.brinkmc.plop.plot.handler.PlotPreviewHandler
import com.brinkmc.plop.plot.handler.PlotShopHandler
import com.brinkmc.plop.plot.handler.PlotSizeHandler
import com.brinkmc.plop.plot.handler.PlotUpgradeHandler
import com.brinkmc.plop.plot.handler.PlotVisitHandler
import com.brinkmc.plop.plot.storage.DatabasePlot
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State

class Plots(override val plugin: Plop): Addon, State {



    // Sub handlers
    lateinit var handler: PlotHandler
    lateinit var databaseHandler: DatabasePlot
    lateinit var visitorHandler: PlotVisitHandler
    lateinit var upgradeHandler: PlotUpgradeHandler
    lateinit var previewHandler: PlotPreviewHandler
    lateinit var factoryHandler: PlotFactoryHandler
    lateinit var claimHandler: PlotClaimHandler
    lateinit var shopHandler: PlotShopHandler
    lateinit var sizeHandler: PlotSizeHandler

    override suspend fun load() {
        handler = PlotHandler(plugin)
        visitorHandler = PlotVisitHandler(plugin)
        upgradeHandler = PlotUpgradeHandler(plugin)
        previewHandler = PlotPreviewHandler(plugin)
        factoryHandler = PlotFactoryHandler(plugin)
        claimHandler = PlotClaimHandler(plugin)
        shopHandler = PlotShopHandler(plugin)
        sizeHandler = PlotSizeHandler(plugin)

        databaseHandler = DatabasePlot(plugin) // Load database last

        listOf(
            handler,
            visitorHandler,
            upgradeHandler,
            previewHandler,
            factoryHandler,
            claimHandler,
            shopHandler,
            sizeHandler,
            databaseHandler
        ).forEach { handler -> (handler as State).load() }
    }

    override suspend fun kill() {
        TODO("Not yet implemented")
    }


}