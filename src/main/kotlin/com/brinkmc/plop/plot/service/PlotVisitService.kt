package com.brinkmc.plop.plot.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.constant.PlotType
import com.brinkmc.plop.plot.dto.Plot
import com.brinkmc.plop.plot.dto.modifier.PlotVisit
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.config.serialisers.Level
import com.brinkmc.plop.shared.constant.MessageKey
import com.brinkmc.plop.shared.constant.ServiceResult
import com.brinkmc.plop.shared.constant.SoundKey
import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.text.get
import kotlin.text.set

class PlotVisitService(override val plugin: Plop): Addon, State {

    private val guildLevels = mutableListOf<Level>()
    private val personalLevels = mutableListOf<Level>()

    val tickDelay: Int = 200 // Every 20 seconds
    var tracking: Boolean = true
    var tracker: Job? = null

    override suspend fun load() {
        configService.plotConfig.getVisitorLimit(PlotType.GUILD).let { guildLevels.addAll(it) } // Add all guild plot size levels
        configService.plotConfig.getVisitorLimit(PlotType.PERSONAL).let { personalLevels.addAll(it) }

        startTracking()
    }

    override suspend fun kill() {
        guildLevels.clear()
        personalLevels.clear()

        stopTracking()
    }

    // Getters

    private suspend fun getPlotVisit(plotId: UUID) = plotService.getPlotVisit(plotId)

    suspend fun getPlotVisitorSize(plotId: UUID): Int? {
        return getPlotVisit(plotId)?.currentVisits?.size
    }

    suspend fun getPlotVisitors(plotId: UUID): List<UUID>? {
        return getPlotVisit(plotId)?.currentVisits?.toList()
    }

    suspend fun getVisitorLimit(plotId: UUID): Int? {
        val plotVisit = getPlotVisit(plotId) ?: return null
        val plotType = plotService.getPlotType(plotId) ?: return null
        return when(plotType) {
            PlotType.GUILD -> guildLevels[plotVisit.level].value
            PlotType.PERSONAL -> personalLevels[plotVisit.level].value
        }
    }

    suspend fun getMaximumVisitors(plotId: UUID): Int? {
        val plotVisit = getPlotVisit(plotId) ?: return null
        val plotType = plotService.getPlotType(plotId) ?: return null
        return when(plotType) {
            PlotType.GUILD -> guildLevels[plotVisit.level].value
            PlotType.PERSONAL -> personalLevels[plotVisit.level].value
        }
    }

    suspend fun getPlotVisitLevel(plotId: UUID): Int? {
        return getPlotVisit(plotId)?.level
    }

    suspend fun canUpgradePlotVisit(plotId: UUID): Boolean {
        val plotType = plotService.getPlotType(plotId) ?: return false
        val plotVisit = getPlotVisit(plotId) ?: return false
        return when(plotType) {
            PlotType.GUILD -> plotVisit.level < guildLevels.size - 1
            PlotType.PERSONAL -> plotVisit.level < personalLevels.size - 1
        }
    }

    suspend fun getCostOfUpgrade(plotId: UUID): Int? {
        val plotType = plotService.getPlotType(plotId) ?: return null
        val plotVisitLevel = getPlotVisitLevel(plotId) ?: return null
        return when(plotType) {
            PlotType.GUILD -> {
                if (!canUpgradePlotVisit(plotId)) return null
                guildLevels[plotVisitLevel + 1].price
            }
            PlotType.PERSONAL -> {
                if (!canUpgradePlotVisit(plotId)) return null
                personalLevels[plotVisitLevel + 1].price
            }
        }
    }

    suspend fun isVisitingEnabled(plotId: UUID): Boolean {
        val plotVisit = getPlotVisit(plotId) ?: return false
        return plotVisit.visitable
    }

    // Setters

    private suspend fun setPlotVisitLevel(plotId: UUID, level: Int) {
        val plotVisit = getPlotVisit(plotId) ?: return
        plotVisit.setLevel(level)
    }

    suspend fun upgradePlotVisit(plotId: UUID): ServiceResult {
        val plotType = plotService.getPlotType(plotId) ?: return ServiceResult.Failure(MessageKey.ERROR, SoundKey.FAILURE)
        val plotVisit = getPlotVisit(plotId) ?: return ServiceResult.Failure(MessageKey.ERROR, SoundKey.FAILURE)
        return when(plotType) {
            PlotType.GUILD if(canUpgradePlotVisit(plotId)) -> {
                setPlotVisitLevel(plotId, plotVisit.level + 1)
                ServiceResult.Success(MessageKey.UPGRADE_SUCCESS, SoundKey.SUCCESS)
            }
            PlotType.PERSONAL if (canUpgradePlotVisit(plotId)) -> {

                setPlotVisitLevel(plotId, plotVisit.level + 1)
                ServiceResult.Success(MessageKey.UPGRADE_SUCCESS, SoundKey.SUCCESS)
            }
            else -> {
                ServiceResult.Failure(MessageKey.REACHED_MAX_UPGRADE_LEVEL, SoundKey.FAILURE)
            }
        }
    }

    fun startTracking() {
        if (tracker?.isActive == true) return // Already running
        tracking = true
        logger.info("Starting visit tracking task")
        tracker = plugin.launch { plugin.asyncScope {
            delay(tickDelay.ticks + 20) // Initial delay to allow players to load in
            while (tracking) {
                val plotToPlayerCount = hashMapOf<UUID, Int?>()
                server.onlinePlayers.forEach { player ->
                    val plotId = playerService.getPlotId(player.uniqueId) ?: return@forEach
                    if (plotService.isPlotMember(plotId, player.uniqueId)) return@forEach // Skip if player is a plot member
                    val plotType = plotService.getPlotType(plotId) ?: return@forEach

                    // Handle enabled visiting
                    if (!plotVisitService.isVisitingEnabled(plotId)) {
                        plotToPlayerCount[plotId] = null
                        // Kick out player if they are on the plot (run command)
                        plugin.consoleCommand(configService.plotConfig.getReturnCommand(plotType), player.uniqueId)
                        return@forEach
                    }

                    // Increment visitor count for the plot
                    val newCount = (plotToPlayerCount[plotId] ?: 0) + 1
                    plotToPlayerCount[plotId] = newCount

                    val visitorLimit = plotVisitService.getVisitorLimit(plotId) ?: return@forEach
                    if (newCount > visitorLimit) {
                        plugin.consoleCommand(configService.plotConfig.getReturnCommand(plotType), player.uniqueId) // Kick out player if limit exceeded (run command)
                        // Kick out player if limit exceeded (run command)
                        plotToPlayerCount[plotId] = visitorLimit // Set to max limit to avoid further checks
                        return@forEach
                    }
                }
                // Use plotToPlayerCount as needed here
                delay(tickDelay.ticks)
            }
        } }
    }

    fun stopTracking() {
        tracking = false
        tracker?.cancel()
        tracker = null
        logger.info("Stopping visit tracking task")
    }
}