package com.brinkmc.plop.shared.base

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.Plots
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.plot.plot.modifier.PlotFactory
import com.brinkmc.plop.plot.plot.modifier.PlotSize
import com.brinkmc.plop.plot.plot.modifier.PlotVisit
import com.brinkmc.plop.shared.config.ConfigReader
import com.brinkmc.plop.shared.config.configs.*
import com.brinkmc.plop.shared.hooks.Economy
import com.brinkmc.plop.shared.storage.HikariManager
import com.brinkmc.plop.shared.util.LocationUtils
import com.brinkmc.plop.shared.util.MessageService
import com.brinkmc.plop.shop.Shops
import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.github.shynixn.mccoroutine.bukkit.scope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.glaremasters.guilds.guild.Guild
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.entity.Player
import org.slf4j.Logger
import java.util.UUID
import kotlin.coroutines.CoroutineContext

internal interface Addon {

    val plugin: Plop

    fun SuspendingJavaPlugin.sync(context: CoroutineContext = minecraftDispatcher, start: CoroutineStart = CoroutineStart.DEFAULT, block: suspend CoroutineScope.() -> Unit): Job {
        if (!scope.isActive) { return Job() }
        return scope.launch(context, start, block)
    }

    fun SuspendingJavaPlugin.async(context: CoroutineContext = asyncDispatcher, start: CoroutineStart = CoroutineStart.DEFAULT, block: suspend CoroutineScope.() -> Unit): Job {
        if (!scope.isActive) { return Job() }
        return scope.launch(context, start, block)
    }

    suspend fun <T> syncScope(block: suspend CoroutineScope.() -> T): T = withContext(plugin.minecraftDispatcher, block)

    suspend fun <T> asyncScope(block: suspend CoroutineScope.() -> T): T = withContext(plugin.asyncDispatcher, block)

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
        get() = plugin.getConfigManager().mainConfig

    val plotConfig: PlotConfig
        get() = plugin.getConfigManager().plotConfig

    val shopConfig: ShopConfig
        get() = plugin.getConfigManager().shopConfig

    val sqlConfig: SQLConfig
        get() = plugin.getConfigManager().sqlConfig

    val totemConfig: TotemConfig
        get() = plugin.getConfigManager().totemConfig

    val economy: Economy
        get() = plugin.hooks.economy

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
        get() = plotConfig.getPlotSizeLevels(this.plotType)[this.level].value ?: -1



    // Provide an easy way to get formatted MiniMessage messages with custom tags also replaced properly
    fun Player.sendMiniMessage(message: String) {
        lang.sendFormattedMessageStr(this, message)
    }

    fun Player.sendMiniMessage(message: Component) {
        lang.sendFormattedMessageComp(this, message)
    }

    // Extension functions for Bukkit
    suspend fun Player.personalPlot(): Plot? {
        // Get a list of all the plots player owns. 1-to-1 relationship
        return plots.handler.getPlotById(uniqueId)
    }

    suspend fun Player.guildPlot(): Plot? {
        return plots.handler.getPlotById(this.guild()?.id)
    }

    suspend fun Player.getPlot(type: PlotType): Plot? {
        return when (type) {
            PlotType.PERSONAL -> personalPlot()
            PlotType.GUILD -> guildPlot()
        }
    }

    suspend fun Player.getPlots(): List<Plot> {
        return plots.handler.getPlotsByMembership(uniqueId)
    }

    // Get guild from player
    fun Player.guild(): Guild? {
        return plugin.hooks.guilds.getGuildFromPlayer(this.uniqueId)
    }

    fun OfflinePlayer.guild(): Guild? {
        return plugin.hooks.guilds.getGuildFromPlayer(this.uniqueId)
    }

    fun UUID.guild(): Guild? {
        return plugin.hooks.guilds.getGuildFromPlayer(this)
    }

    fun UUID.player(): Player? {
        return server.getPlayer(this)
    }

    suspend fun UUID.plot(): Plot? {
        return plots.handler.getPlotById(this)
    }

    suspend fun Guild.plot(): Plot? {
        return plots.handler.getPlotById(this.id)
    }

    // Update border

    suspend fun Player.updateBorder() {
        plots.handler.updateBorder(uniqueId)
    }

    fun World.isPlotWorld(): Boolean {
        return plots.handler.getPlotWorlds().contains(this)
    }

    // Location check for player

    suspend fun Player.inPlot(): Boolean {
        return (plots.handler.getPlotFromLocation(location)?.plotId == player?.uniqueId) || (plots.handler.getPlotFromLocation(location)?.plotId == player?.guild()?.id)
    }

    suspend fun Player.getCurrentPlot(): Plot? {
        return plots.handler.getPlotFromLocation(location)
    }

    suspend fun Location.getCurrentPlot(): Plot? {
        return plots.handler.getPlotFromLocation(this)
    }

    // Locations

    suspend fun Location.getSafeDestination(): Location? {
        return plugin.locationUtils.getSafe(this)
    }
}