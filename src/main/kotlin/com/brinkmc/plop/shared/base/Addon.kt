package com.brinkmc.plop.shared.base

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.Plots
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.plot.plot.base.PlotOwner
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.plot.plot.modifier.PlotFactory
import com.brinkmc.plop.plot.plot.modifier.PlotShop
import com.brinkmc.plop.plot.plot.modifier.PlotSize
import com.brinkmc.plop.plot.plot.modifier.PlotTotem
import com.brinkmc.plop.shared.config.ConfigReader
import com.brinkmc.plop.shared.config.configs.*
import com.brinkmc.plop.shared.hooks.Economy
import com.brinkmc.plop.shared.hooks.Locals.world
import com.brinkmc.plop.shared.storage.HikariManager
import com.brinkmc.plop.shared.util.message.MessageService
import com.brinkmc.plop.shop.shop.Shop
import com.brinkmc.plop.shop.Shops
import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.github.shynixn.mccoroutine.bukkit.scope
import io.lumine.mythic.api.adapters.AbstractLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.glaremasters.guilds.guild.Guild
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.apache.http.util.Args
import org.bukkit.*
import org.bukkit.block.Chest
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataType
import org.slf4j.Logger
import java.util.UUID
import kotlin.coroutines.CoroutineContext

internal interface Addon {

    val plugin: Plop

    fun SuspendingJavaPlugin.sync(context: CoroutineContext = minecraftDispatcher, start: CoroutineStart = CoroutineStart.DEFAULT, block: suspend CoroutineScope.() -> Unit): Job {
        return this.launch(context, start, block)
    }

    fun SuspendingJavaPlugin.async(context: CoroutineContext = asyncDispatcher, start: CoroutineStart = CoroutineStart.DEFAULT, block: suspend CoroutineScope.() -> Unit): Job {
        return this.launch(context, start, block)
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

    // PlotTotemHandler extenisons
    val PlotTotem.limit: Int
        get() = plots.totemHandler.getCurrentTotemLimit(this.plotType, this.level)

    val PlotTotem.max: Int
        get() = plots.totemHandler.getMaximumTotemLimit(this.plotType)

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
        return listOfNotNull(
            plotConfig.getPlotWorld(PlotType.PERSONAL).world(),
            plotConfig.getPlotWorld(PlotType.GUILD).world()
        ).contains(this)
    }

    // Location check for player

    suspend fun Player.getCurrentPlot(): Plot? {
        return plugin.playerTracker.locations.get(this)
    }

    suspend fun Location.getCurrentPlot(): Plot? {
        return plots.handler.getPlotFromLocation(this)
    }

    suspend fun AbstractLocation.getCurrentPlot(): Plot? {
        return plots.handler.getPlotFromLocation(this.toLocation())
    }

    suspend fun AbstractLocation.toLocation(): Location {
        return Location(Bukkit.getWorld(world.name), x, y, z)
    }

    // Locations

    suspend fun Chest.toShop(): Shop? {
        val shopId = syncScope {
            this@toShop.persistentDataContainer.get(shops.handler.key, PersistentDataType.STRING)
        }
        if (shopId == null) return null
        return shops.handler.getShop(UUID.fromString(shopId))
    }

    suspend fun Shop.chest(): Chest {
        return syncScope {
            return@syncScope location.block.state as Chest
        }
    }

    suspend fun Location.getSafeDestination(): Location? {
        return plugin.locationUtils.getSafe(this)
    }

    suspend fun List<Location>.getClosest(location: Location): Location? {
        return this.minByOrNull { it.distanceSquared(location) }
    }

    // Provide an easy way to get formatted MiniMessage messages with custom tags also replaced properly

    fun Player.sendMiniMessage(message: String, shop: Shop? = null, plot: Plot? = null, vararg args: TagResolver) {
        val component = lang.deserialise(message, player = this, shop = shop, plot = plot, args = args)
        sendMessage(component)
    }

    // GUI Extensions

    fun ItemStack.name(name: String, player: Player? = null, shop: Shop? = null, plot: Plot? = null, vararg args: TagResolver): ItemStack {
        itemMeta = itemMeta.also { meta ->
            meta.displayName(lang.deserialise(name, player = player, shop = shop, plot = plot, args = args))
        }
        return this
    }

    fun ItemStack.description(description: String, player: Player? = null, shop: Shop? = null, plot: Plot? = null, vararg args: TagResolver): ItemStack {
        itemMeta = itemMeta.also { meta ->
            meta.lore(listOf(lang.deserialise(description, player = player, shop = shop, plot = plot, args = args)))
        }
        return this
    }

    fun ItemStack.setSkull(owner: PlotOwner?): ItemStack {
        val meta = itemMeta as SkullMeta
        meta.playerProfile = owner?.getSkull()
        itemMeta = meta
        return this
    }

    // Shop

    suspend fun Player.personalShops(): List<Shop> {
        val plot = personalPlot() ?: return emptyList()
        return shops.handler.getShops(plot.plotId)
    }

    suspend fun Guild.guildShops(): List<Shop> {
        val plot = this.plot() ?: return emptyList()
        return shops.handler.getShops(plot.plotId)
    }

    suspend fun UUID.shop(): Shop? {
        return shops.handler.getShop(this)
    }

    suspend fun Plot.getShops(): List<Shop> {
        return shops.handler.getShops(this.plotId)
    }

    // Shop inventory utils

    // Get the amount of an item in an inventory, however divide by the item's amount
    fun Inventory.getAmountOf(item: ItemStack): Int {
        return contents.filterNotNull().filter { it.isSimilar(item) }.sumOf{ it.amount }.div(item.amount)
    }

}

fun ClickType.isDrop(): Boolean {
    return this == ClickType.DROP || this == ClickType.CONTROL_DROP
}
