package com.brinkmc.plop.shared.base

import com.brinkmc.plop.Plop
import com.brinkmc.plop.factory.Factories
import com.brinkmc.plop.factory.service.FactoryActionService
import com.brinkmc.plop.factory.service.FactoryAugmentService
import com.brinkmc.plop.factory.service.FactoryService
import com.brinkmc.plop.plot.Plots
import com.brinkmc.plop.plot.service.PlotClaimService
import com.brinkmc.plop.plot.service.PlotFactoryService
import com.brinkmc.plop.plot.service.PlotFuelerService
import com.brinkmc.plop.plot.service.PlotLayoutService
import com.brinkmc.plop.plot.service.PlotNexusService
import com.brinkmc.plop.plot.service.PlotPreviewService
import com.brinkmc.plop.plot.service.PlotService
import com.brinkmc.plop.plot.service.PlotShopService
import com.brinkmc.plop.plot.service.PlotSizeService
import com.brinkmc.plop.plot.service.PlotTotemService
import com.brinkmc.plop.plot.service.PlotUpgradeService
import com.brinkmc.plop.plot.service.PlotVisitService
import com.brinkmc.plop.shared.service.ConfigService
import com.brinkmc.plop.shared.db.HikariManager
import com.brinkmc.plop.shared.service.HookService
import com.brinkmc.plop.shared.hook.api.PlayerTracker
import com.brinkmc.plop.shared.service.DesignService
import com.brinkmc.plop.shared.service.MenuService
import com.brinkmc.plop.shared.service.PlayerService
import com.brinkmc.plop.shop.Shops
import com.brinkmc.plop.shop.service.ShopAccessService
import com.brinkmc.plop.shop.service.ShopCreationService
import com.brinkmc.plop.shop.service.ShopService
import com.brinkmc.plop.shop.service.ShopStockService
import com.brinkmc.plop.shop.service.ShopTransactionService
import com.google.gson.Gson
import net.kyori.adventure.audience.Audiences
import org.bukkit.*
import org.slf4j.Logger

internal interface Addon {

    val plugin: Plop

    val server: Server
        get() = plugin.server

    val logger: Logger
        get() = plugin.slF4JLogger

    val DB: HikariManager
        get() = plugin.DB

    val gson: Gson
        get() = plugin.gson

    val menuService: MenuService
        get() = plugin.menuService

    val configService: ConfigService
        get() = plugin.configService

    val hookService: HookService
        get() = plugin.hookService

    val designService: DesignService
        get() = plugin.designService

    val playerService: PlayerService
        get() = plugin.playerService


    // Plot handlers
    val plots: Plots
        get() = plugin.plots

    val plotService: PlotService
        get() = plugin.plots.plotService

    val plotVisitService: PlotVisitService
        get() = plugin.plots.plotVisitService

    val plotUpgradeService: PlotUpgradeService
        get() = plugin.plots.plotUpgradeService

    val plotPreviewService: PlotPreviewService
        get() = plugin.plots.plotPreviewService

    val plotTotemService: PlotTotemService
        get() = plugin.plots.plotTotemService

    val plotFactoryService: PlotFactoryService
        get() = plugin.plots.plotFactoryService

    val plotClaimService: PlotClaimService
        get() = plugin.plots.plotClaimService

    val plotShopService: PlotShopService
        get() = plugin.plots.plotShopService

    val plotSizeService: PlotSizeService
        get() = plugin.plots.plotSizeService

    val plotNexusService: PlotNexusService
        get() = plugin.plots.plotNexusService

    val plotFuelerService: PlotFuelerService
        get() = plugin.plots.plotFuelerService

    val plotLayoutService: PlotLayoutService
        get() = plugin.plots.plotLayoutService


    // Shop handlers
    val shops: Shops
        get() = plugin.shops

    val shopStockService: ShopStockService
        get() = plugin.shops.shopStockService

    val shopCreationService: ShopCreationService
        get() = plugin.shops.shopCreationService

    val shopTransactionService: ShopTransactionService
        get() = plugin.shops.shopTransactionService

    val shopAccessService: ShopAccessService
        get() = plugin.shops.shopAccessService

    val shopService: ShopService
        get() = plugin.shops.shopService

    // Factory handlers
    val factories: Factories
        get() = plugin.factories

    val factoryService: FactoryService
        get() = plugin.factories.factoryService

    val factoryActionService: FactoryActionService
        get() = plugin.factories.factoryActionService

    val factoryAugmentService: FactoryAugmentService
        get() = plugin.factories.factoryAugmentService

}