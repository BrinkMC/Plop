package com.brinkmc.plop.shared.base

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.Plots
import com.brinkmc.plop.plot.handler.PlotUpgradeHandler
import com.brinkmc.plop.shop.Shops
import org.bukkit.Server

internal interface Addon {

    val plugin: Plop

    val server: Server
        get() = plugin.server

    val logger: org.slf4j.Logger
        get() = plugin.slF4JLogger

    val plots: Plots
        get() = plugin.plots

    val shops: Shops
        get() = plugin.shops

    val plotUpgradeHandler: PlotUpgradeHandler
        get() = checkNotNull(plots.plotUpgradeHandler)
}