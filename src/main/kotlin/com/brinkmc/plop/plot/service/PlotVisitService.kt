package com.brinkmc.plop.plot.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.constant.PlotType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.config.serialisers.Level
import com.brinkmc.plop.shared.constant.MessageKey
import com.brinkmc.plop.shared.constant.ServiceResult
import com.brinkmc.plop.shared.constant.SoundKey
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds

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

    suspend fun getPlotVisitorCount(plotId: UUID): Int? {
        return getPlotVisit(plotId)?.currentVisits?.size
    }

    suspend fun getPlotVisitors(plotId: UUID): List<UUID>? {
        return getPlotVisit(plotId)?.currentVisits?.toList()
    }

    /*
        * Returns the visitor limit for the plot based on its type and visit level. Returns null if plot visit or type is not found.
     */
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

    suspend fun canVisit(plotId: UUID): ServiceResult {

        if (!isVisitingEnabled(plotId)) {
            return ServiceResult.Failure(MessageKey.PLOT_CLOSED, SoundKey.FAILURE)
        }

        // Maximum number of players compared to players currently in the plot
        val visitorLimit = getVisitorLimit(plotId) ?: return ServiceResult.Failure(MessageKey.ERROR, SoundKey.FAILURE)
        val currentVisitors = getPlotVisitorCount(plotId) ?: return ServiceResult.Failure(MessageKey.ERROR, SoundKey.FAILURE)
        
        if (visitorLimit >= currentVisitors) {
            return ServiceResult.Failure(MessageKey.PLOT_FULL, SoundKey.FAILURE)
        }
        
        return ServiceResult.Success()
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
                ServiceResult.Failure(MessageKey.MAX_UPGRADE_REACHED, SoundKey.FAILURE)
            }
        }
    }

    suspend fun togglePlotVisit(playerId: UUID): ServiceResult {
        val plotId = playerService.getPlotId(playerId) ?: return ServiceResult.Failure(MessageKey.NO_PLOT, SoundKey.FAILURE)
        val isOwner = plotService.isPlotOwner(plotId, playerId)

        if (!isOwner) {
            return ServiceResult.Failure(MessageKey.NOT_OWNER, SoundKey.FAILURE)
        }

        val plotVisit = getPlotVisit(plotId) ?: return ServiceResult.Failure(MessageKey.ERROR, SoundKey.FAILURE)

        plotVisit.setVisitable(!isVisitingEnabled(plotId))
        return ServiceResult.Success(MessageKey.PLOT_TOGGLE_VISIT, SoundKey.SUCCESS)
    }

    suspend fun addVisitor(plotId: UUID, visitorId: UUID) {
        val plotVisit = getPlotVisit(plotId) ?: return
        plotVisit.addVisitor(visitorId)
        return
    }

    suspend fun removeVisitor(plotId: UUID, visitorId: UUID) {
        val plotVisit = getPlotVisit(plotId) ?: return
        plotVisit.removeVisitor(visitorId)
        return
    }

    // Actions

    suspend fun teleportToHome(playerId: UUID, plotType: PlotType?): ServiceResult {
        val request = plotType ?: menuService.plotTypeMenu.request(playerId, null, playerId)

        val plotId = when (request) {
            PlotType.PERSONAL -> if (plotService.hasPersonalPlot(playerId)) plotService.getPlotId(playerId, PlotType.PERSONAL) else null
            PlotType.GUILD -> if (plotService.hasGuildPlot(playerId)) plotService.getPlotId(playerId, PlotType.GUILD) else null
            else -> null
        }

        if (plotId == null) {
            return ServiceResult.Failure(MessageKey.TELEPORT_CHOOSE_PLOT_TYPE_ERROR, SoundKey.FAILURE)
        }

        val plotHomeLocation = plotClaimService.getPlotHome(plotId) ?: return ServiceResult.Failure(MessageKey.ERROR, SoundKey.FAILURE)

        playerService.performTeleportCountdown(playerId, 5, plotHomeLocation)
        return ServiceResult.Success()
    }

    suspend fun visitPlot(playerId: UUID, targetId: UUID, plotType: PlotType?): ServiceResult {
        val request = plotType ?: menuService.plotTypeMenu.request(playerId, null, targetId)

        val plotId = when (request) {
            PlotType.PERSONAL -> if (plotService.hasPersonalPlot(targetId)) plotService.getPlotId(targetId, PlotType.PERSONAL) else null
            PlotType.GUILD -> if (plotService.hasGuildPlot(targetId)) plotService.getPlotId(targetId, PlotType.GUILD) else null
            else -> null
        }

        if (plotId == null) {
            return ServiceResult.Failure(MessageKey.TELEPORT_CHOOSE_PLOT_TYPE_ERROR, SoundKey.FAILURE)
        }

        // Visiting is disabled
        when (val result = plotVisitService.canVisit(plotId)) {
            is ServiceResult.Success -> {
                val plotVisitLocation = plotClaimService.getPlotVisit(plotId) ?: return ServiceResult.Failure(MessageKey.ERROR, SoundKey.FAILURE)
                playerService.performTeleportCountdown(playerId, 5, plotVisitLocation)
                return ServiceResult.Success()
            }
            is ServiceResult.Failure -> {
                return result
            }
        }
    }

    fun startTracking() {
        if (tracker?.isActive == true) return // Already running
        tracking = true

        logger.info("Starting visit tracking task")
        tracker = plugin.launch { plugin.asyncScope {
            delay((tickDelay.ticks + 20).milliseconds) // Initial delay to allow players to load in

            // Loop
            while (tracking) {

                val currentPlayers = server.onlinePlayers.toList()

                val plotOccupants = currentPlayers.mapNotNull {
                    val plotId = playerService.getPlotId(it.uniqueId) ?: return@mapNotNull null
                    if (plotService.isPlotMember(plotId, it.uniqueId)) return@mapNotNull null

                    plotId to it.uniqueId
                }.groupBy({ it.first }, { it.second })

                for ((plotId, occupants) in plotOccupants) {
                    val isOpen = plotVisitService.isVisitingEnabled(plotId)
                    val limit = plotVisitService.getVisitorLimit(plotId) ?: -1

                    occupants.forEachIndexed { index, occupant ->
                        val isOverLimit = limit != -1 && index >= limit

                        if (!isOpen || isOverLimit) {
                            removeVisitor(plotId, occupant)
                            ejectPlayer(plotId, occupant)
                        } else {
                            addVisitor(plotId, occupant)
                        }
                    }
                }

                delay(tickDelay.ticks.milliseconds)
            }
        } }
    }

    suspend fun ejectPlayer(plotId: UUID, occupant: UUID): ServiceResult {
        val command = configService.plotConfig.getReturnCommand(plotService.getPlotType(plotId) ?: return ServiceResult.Failure(MessageKey.ERROR, SoundKey.FAILURE))
        plugin.consoleCommand(command, occupant)
        return ServiceResult.Success(MessageKey.PLOT_FULL, SoundKey.TELEPORT)
    }


    fun stopTracking() {
        tracking = false
        tracker?.cancel()
        tracker = null
        logger.info("Stopping visit tracking task")
    }
}