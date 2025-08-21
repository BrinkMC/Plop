package com.brinkmc.plop.plot.handler

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.plot.storage.PlotCache
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.hook.api.Locals.world
import com.brinkmc.plop.shared.util.BukkitUtils.player
import com.brinkmc.plop.shared.util.CoroutineUtils.async
import com.brinkmc.plop.shared.util.LocationString.fullString
import com.brinkmc.plop.shared.util.design.enums.MessageKey
import com.brinkmc.plop.shared.util.design.enums.SoundKey
import com.brinkmc.plop.shared.util.plot.PlotType
import com.github.yannicklamprecht.worldborder.api.WorldBorderApi
import kotlinx.coroutines.delay
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import java.util.*
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds

class PlotHandler(override val plugin: Plop): Addon, State  {

    private lateinit var plotCache: PlotCache
    private lateinit var locationCache: HashMap<String, UUID>
    private lateinit var borderAPI: WorldBorderApi // Late init as it is not available on startup

    override suspend fun load() { plugin.syncScope {
        plotCache = PlotCache(plugin) // Load the cache + database
        plotCache.load()

        borderAPI = server.servicesManager.getRegistration<WorldBorderApi?>(WorldBorderApi::class.java)?.provider ?: run {
            logger.error("Failed to get WorldBorderAPI")
            return@syncScope
        }
    }}


    override suspend fun kill() {
        plotCache.kill()
    }

    // Search functions
    suspend fun getPlotById(plotId: UUID?): Plot? {
        if (plotId == null) return null
        return plotCache.getPlot(plotId)
    }

    suspend fun getPlotsByMembership(player: UUID): List<Plot> { // Gets all plots someone is in
        return listOfNotNull(
            getPlotById(player),
            getPlotById(hookHandler.guilds.getGuild(player)?.id)
        )
    }

    suspend fun getPlotFromLocation(location: Location): Plot? { return plugin.asyncScope {
        val locationKey = location.fullString(false)
        val cachedPlotId = locationCache[locationKey]
        if (cachedPlotId != null) {
            return@asyncScope getPlotById(cachedPlotId)
        }

        val worldGuardRegions = plugin.hookHandler.worldGuard.getRegions(location)

        if (worldGuardRegions?.size != 1) {
            return@asyncScope null
        }
        val regionIdString = worldGuardRegions.first()?.id ?: return@asyncScope null
        val plotId = UUID.fromString(regionIdString)

        locationCache[locationKey] = plotId
        return@asyncScope getPlotById(plotId)
    } }

    suspend fun hasGuildPlot(player: UUID): Boolean {
        return getPlotById(hookHandler.guilds.getGuild(player)?.id) != null
    }

    suspend fun hasPersonalPlot(player: UUID): Boolean {
        return getPlotById(player) != null
    }

    suspend fun isPlotWorld(world: World): Boolean {
        return listOfNotNull(
            configHandler.plotConfig.getPlotWorld(PlotType.PERSONAL).world(),
            configHandler.plotConfig.getPlotWorld(PlotType.GUILD).world()
        ).contains(world)
    }

    suspend fun addPlot(plot: Plot) {
        plotCache.addPlot(plot)
    }

    suspend fun deletePlot(plot: Plot) {
        plotCache.deletePlot(plot)
    }
}