package com.brinkmc.plop.shared.base

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.Plots
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.plot.plot.modifier.PlotFactory
import com.brinkmc.plop.plot.plot.modifier.PlotSize
import com.brinkmc.plop.plot.plot.modifier.PlotVisit
import com.brinkmc.plop.shared.config.ConfigReader
import com.brinkmc.plop.shared.config.configs.*
import com.brinkmc.plop.shared.storage.HikariManager
import com.brinkmc.plop.shared.util.MessageService
import com.brinkmc.plop.shop.Shops
import kotlinx.coroutines.sync.Mutex
import net.kyori.adventure.text.Component
import org.bukkit.Server
import org.bukkit.entity.Player
import org.slf4j.Logger

internal interface Addon {

    val plugin: Plop

    val mutex: Mutex
        get() = plugin.mutex

    val server: Server
        get() = plugin.server

    val logger: Logger
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

    // Extension functions

    // PlotSizeHandler extensions
    val PlotSize.current: Int
        get() = plots.sizeHandler.getCurrentPlotSize(this.plotType, this.level)

    val PlotSize.max: Int
        get() = plots.sizeHandler.getMaximumPlotSize(this.plotType)

    // PlotFactoryHandler extensions
    val PlotFactory.limit: Int
        get() = plots.factoryHandler.getCurrentFactoryLimit(this.plotType, this.level)

    val PlotFactory.max: Int
        get() = plots.factoryHandler.getMaximumFactoryLimit(this.plotType)

    val PlotVisit.amount: Int
        get() = plotConfig.getPlotSizeLevels(this.plotType)[this.level]



    // Provide an easy way to get formatted MiniMessage messages with custom tags also replaced properly
    fun Player.sendFormattedMessage(message: String) {
        lang.sendFormattedMessage(this, message)
    }

    fun Player.sendFormattedMessage(message: Component) {
        lang.sendFormattedMessage(this, message)
    }

    // Extension functions for Bukkit
    fun Player.personalPlot(): Plot? {
        // Get a list of all the plots player owns. 1-to-1 relationship
        return plots.handler.getPlotByOwner(uniqueId)
    }

    fun Player.guildPlot(): Plot? {
        return plots.handler.getPlotByOwner(plugin.hooks.guilds.guildAPI.getGuildByPlayerId(this.uniqueId)?.id)
    }
}