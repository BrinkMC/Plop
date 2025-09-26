package com.brinkmc.plop.plot.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.dto.Plot
import com.brinkmc.plop.plot.dao.PlotCache
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.hook.api.Locals.world
import com.brinkmc.plop.shared.util.LocationString.fullString
import com.brinkmc.plop.plot.constant.PlotType
import com.brinkmc.plop.plot.dto.modifier.PlotClaim
import com.brinkmc.plop.plot.dto.modifier.PlotFactory
import com.brinkmc.plop.plot.dto.modifier.PlotFueler
import com.brinkmc.plop.plot.dto.modifier.PlotNexus
import com.brinkmc.plop.plot.dto.modifier.PlotShop
import com.brinkmc.plop.plot.dto.modifier.PlotSize
import com.brinkmc.plop.plot.dto.modifier.PlotTotem
import com.brinkmc.plop.plot.dto.modifier.PlotVisit
import com.brinkmc.plop.shop.dto.Shop
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.yannicklamprecht.worldborder.api.WorldBorderApi
import com.sksamuel.aedile.core.asCache
import com.sksamuel.aedile.core.asLoadingCache
import com.sksamuel.aedile.core.expireAfterAccess
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Location
import org.bukkit.World
import java.util.*
import kotlin.time.Duration.Companion.minutes

class PlotService(override val plugin: Plop): Addon, State  {

    private val plotCache: PlotCache = PlotCache(plugin) // Cache of all plots

     // Late init as it is not available on startup

    override suspend fun load() { plugin.syncScope {
        plotCache.load()
    }}


    override suspend fun kill() {
        plotCache.kill()
    }

    suspend fun getPlotWorld(plotId: UUID): World {
        val plot = plotCache.getPlot(plotId) ?: throw RuntimeException("Plot not found")
        return getPlotWorld(plot.type)
    }

    fun getPlotWorld(plotType: PlotType): World {
        return configService.plotConfig.getPlotWorld(plotType).world() ?: throw RuntimeException("Plot world not found")
    }

    fun isPlotWorld(world: World): Boolean {
        return listOfNotNull(
            configService.plotConfig.getPlotWorld(PlotType.PERSONAL).world(),
            configService.plotConfig.getPlotWorld(PlotType.GUILD).world()
        ).contains(world)
    }

    private suspend fun getPlots(playerId: UUID): Map<UUID,Plot?> { // Get all shops which have plotId of some value
        return plotCache.getPlots().filter {
            it.value?.id == playerId || it.value?.id == hookService.guilds.getGuildFromPlayer(playerId)
        }
    }

    private suspend fun getPlot(plotId: UUID): Plot? {
        return plotCache.getPlot(plotId)
    }

    // Public functions

    suspend fun hasGuildPlot(playerId: UUID): Boolean {
        val guild = hookService.guilds.getGuildFromPlayer(playerId)?.id ?: return false
        return getPlot(guild) != null
    }

    suspend fun hasPersonalPlot(playerId: UUID): Boolean {
        return getPlot(playerId) != null
    }

    suspend fun getPlotIdFromLocation(location: Location): UUID? {
        return plotCache.getPlotId(location)
    }

    suspend fun addPlot(plot: Plot) {
        plotCache.addPlot(plot)
    }

    suspend fun deletePlot(plot: Plot) {
        plotCache.deletePlot(plot)
    }

    // Getters

    suspend fun getPlotOwnerDisplayName(plotId: UUID): String? {
        val plotType = getPlotType(plotId) ?: return null
        return when (plotType) {
            PlotType.PERSONAL -> {
                // Player id is same as plot id
                val owner = playerService.getDisplayName(plotId) ?: return null
                PlainTextComponentSerializer.plainText().serialize(owner)
            }
            PlotType.GUILD -> hookService.guilds.getGuild(plotId)?.name
        }
    }

    suspend fun getPlotType(plotId: UUID): PlotType? {
        val plot = getPlot(plotId) ?: return null
        return plot.type
    }

    suspend fun getPlotMembers(plotId: UUID): List<UUID?> {
        val plot = getPlot(plotId) ?: return emptyList()
        return when (plot.type) {
            PlotType.PERSONAL -> listOf(plot.id)
            PlotType.GUILD -> hookService.guilds.getGuild(plot.id)?.members?.map { it.uuid } ?: emptyList()
        }
    }

    suspend fun getPlotNexus(plotId: UUID): PlotNexus? {
        val plot = getPlot(plotId) ?: return null
        return plot.nexus
    }

    suspend fun getPlotClaim(plotId: UUID): PlotClaim? {
        val plot = getPlot(plotId) ?: return null
        return plot.claim
    }

    suspend fun getPlotVisit(plotId: UUID): PlotVisit? {
        val plot = getPlot(plotId) ?: return null
        return plot.visit
    }

    suspend fun getPlotSize(plotId: UUID): PlotSize? {
        val plot = getPlot(plotId) ?: return null
        return plot.size
    }

    suspend fun getPlotFactory(plotId: UUID): PlotFactory? {
        val plot = getPlot(plotId) ?: return null
        return plot.factory
    }

    suspend fun getPlotShop(plotId: UUID): PlotShop? {
        val plot = getPlot(plotId) ?: return null
        return plot.shop
    }

    suspend fun getPlotTotem(plotId: UUID): PlotTotem? {
        val plot = getPlot(plotId) ?: return null
        return plot.totem
    }

    suspend fun getPlotFueler(plotId: UUID): PlotFueler? {
        val plot = getPlot(plotId) ?: return null
        return plot.fueler
    }




}