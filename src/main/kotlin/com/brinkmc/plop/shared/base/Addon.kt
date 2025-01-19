package com.brinkmc.plop.shared.base

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.Plots
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.shared.config.ConfigReader
import com.brinkmc.plop.shared.config.configs.*
import com.brinkmc.plop.shared.storage.HikariManager
import com.brinkmc.plop.shared.util.MessageService
import com.brinkmc.plop.shop.Shops
import org.bukkit.Server
import org.bukkit.entity.Player

internal interface Addon {

    val plugin: Plop

    val server: Server
        get() = plugin.server

    val logger: org.slf4j.Logger
        get() = plugin.slF4JLogger

    val DB: HikariManager
        get() = plugin.DB

    val plots: Plots
        get() = plugin.plots

    val shops: Shops
        get() = plugin.shops

    val configManager: ConfigReader
        get() = plugin.getConfigManager()

    val lang: MessageService
        get() = plugin.getMessageService()

    val mainConfig: MainConfig
        get() = plugin.configs.mainConfig

    val plotConfig: PlotConfig
        get() = plugin.configs.plotConfig

    val shopConfig: ShopConfig
        get() = plugin.configs.shopConfig

    val SQLConfig: SQLConfig
        get() = plugin.configs.SQLConfig

    val totemConfig: TotemConfig
        get() = plugin.configs.totemConfig

    // EXTENSION FUNCTIONS

    fun Plot.getCurrentPlotSize(): Int { // Handle extension functions inside Addon class
        return plots.plotSizeHandler.getCurrentPlotSize(this)
    }

    fun Plot.getPlotSizeLimit(): Int { // Handle plot size limits
        return plots.plotSizeHandler.getPlotSizeLimit(this)
    }

    // Provide an easy way to get formatted MiniMessage messages with custom tags also replaced properly
    fun Player.sendFormattedMessage(message: String) {
        with (lang) { sendFormattedMessage(message) }
    }
}