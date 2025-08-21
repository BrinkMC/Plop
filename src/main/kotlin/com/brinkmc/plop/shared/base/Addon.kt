package com.brinkmc.plop.shared.base

import com.brinkmc.plop.Plop
import com.brinkmc.plop.factory.Factories
import com.brinkmc.plop.plot.Plots
import com.brinkmc.plop.shared.config.ConfigHandler
import com.brinkmc.plop.shared.db.HikariManager
import com.brinkmc.plop.shared.gui.MenuHandler
import com.brinkmc.plop.shared.hook.HookHandler
import com.brinkmc.plop.shared.hook.api.PlayerTracker
import com.brinkmc.plop.shared.util.design.DesignHandler
import com.brinkmc.plop.shop.Shops
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

    val menuHandler: MenuHandler
        get() = plugin.menuHandler

    val hookHandler: HookHandler
        get() = plugin.hookHandler

    val configHandler: ConfigHandler
        get() = plugin.configHandler

    val messages: DesignHandler
        get() = plugin.designHandler

    val audiences: Audiences
        get() = plugin.audiences

    val playerTracker: PlayerTracker
        get() = plugin.hookHandler.playerTracker

    // Plot handlers
    val plots: Plots
        get() = plugin.plots

    val plotHandler: Plots
        get() = plugin.plots.plotHandler

    val plotFactoryHandler: Plots
        get() = plugin.plots.plotFactoryHandler

    val plotFuelerHandler: Plots
        get() = plugin.plots.plotFuelerHandler

    val plotClaimHandler: Plots
        get() = plugin.plots.plotClaimHandler

    val plotPreviewHandler: Plots
        get() = plugin.plots.plotPreviewHandler

    val plotShopHandler: Plots
        get() = plugin.plots.plotShopHandler

    val plotSizeHandler: Plots
        get() = plugin.plots.plotSizeHandler

    val plotTotemHandler: Plots
        get() = plugin.plots.plotTotemHandler

    val plotUpgradeHandler: Plots
        get() = plugin.plots.plotUpgradeHandler

    val plotVisitHandler: Plots
        get() = plugin.plots.plotVisitHandler

    // Shop handlers
    val shops: Shops
        get() = plugin.shops

    val shopTransactionHandler: Shops
        get() = plugin.shops.shopTransactionHandler

    val shopCreationHandler: Shops
        get() = plugin.shops.shopCreationHandler

    // Factory handlers
    val factories: Factories
        get() = plugin.factories
}