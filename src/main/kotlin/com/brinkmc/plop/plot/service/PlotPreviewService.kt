package com.brinkmc.plop.plot.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.constant.PlotType
import com.brinkmc.plop.plot.constant.PreviewResult
import com.brinkmc.plop.plot.dto.PlotPreview
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.event.player.PlayerTeleportEvent
import java.util.UUID

/*
Keep track of all active preview instances
Ensure that data is saved e.t.c
 */

class PlotPreviewService(override val plugin: Plop): Addon, State {


    private val previews = hashMapOf<UUID, PlotPreview>()

    override suspend fun load() {
        logger.info("Loading plot previews...")
    }

    override suspend fun kill() {
        for (preview in previews) {
            val playerId = preview.key
            returnTeleport(playerId)
            menuService.previewHotbar.close(playerId)
            returnInventory(playerId)
        }

        previews.clear()
    }

    // Combined checks

    private suspend fun guildChecks(playerId: UUID): PreviewResult {
        // Guild checks
        val guild = hookService.guilds.getGuildFromPlayer(playerId)
        val guildMaster = guild?.guildMaster?.uuid
        val guildSize = guild?.size ?: 0
        val minGuildSize = configService.plotConfig.guildConfig.minSize

        if (plotService.hasGuildPlot(playerId)) return PreviewResult.GUILD_HAS_PLOT
        if (guild == null) return PreviewResult.NO_GUILD
        if (guildMaster == playerId) return PreviewResult.NOT_GUILD_MASTER
        if (guildSize < minGuildSize) return PreviewResult.GUILD_TOO_SMALL

        return PreviewResult.SUCCESS
    }

    private suspend fun personalChecks(playerId: UUID): PreviewResult {
        // Personal checks
        if (plotService.hasPersonalPlot(playerId)) return PreviewResult.PLAYER_HAS_PLOT
        return PreviewResult.SUCCESS
    }


    // Getters

    private fun getPreview(playerId: UUID): PlotPreview? {
        return previews[playerId]
    }

    fun getPreviewPlot(playerId: UUID): Location? {
        val preview = getPreview(playerId) ?: return null
        return preview.previewPlot.value.toLocation()
    }

    fun getPlotType(playerId: UUID): PlotType {
        return if (getPreview(playerId)?.world == configService.plotConfig.personalConfig.personalPlotWorld) {
            PlotType.PERSONAL
        } else {
            PlotType.GUILD
        }
    }

    // Setters

    fun setPreviewPlot(playerId: UUID, preview: PlotPreview) {
        previews[playerId] = preview
    }

    // Actions

    private suspend fun nextPlot(playerId: UUID) {
        val preview = getPreview(playerId) ?: return
        toggleFree(playerId, true)

        val nextPlot = plotLayoutService.getNextFreePlot(preview.previewPlot, getPlotType(playerId))
        preview.setPreviewPlot(nextPlot)
        toggleFree(playerId, false)
    }

    private suspend fun previousPlot(playerId: UUID) {
        val preview = getPreview(playerId) ?: return
        toggleFree(playerId, true)

        val previousPlot = plotLayoutService.getPreviousFreePlot(preview.previewPlot, getPlotType(playerId))
        preview.setPreviewPlot(previousPlot)
        toggleFree(playerId, false)
    }

    fun toggleFree(playerId: UUID, free: Boolean) {
        val preview = getPreview(playerId) ?: return
        preview.previewPlot.value.free = free
    }

    fun returnTeleport(playerId: UUID) {
        val preview = getPreview(playerId) ?: return
        playerService.teleport(playerId, preview.savedLocation, PlayerTeleportEvent.TeleportCause.PLUGIN)
    }

    fun returnInventory(playerId: UUID) {
        val preview = getPreview(playerId) ?: return
        playerService.setInventory(playerId, preview.savedInventory)
    }

    fun teleportToPlot(playerId: UUID) {
        val previewPlot = getPreviewPlot(playerId) ?: return
        val location = previewPlot.clone().add(0.0, 1.0, 0.0)

        playerService.teleport(playerId, location, PlayerTeleportEvent.TeleportCause.COMMAND)
        toggleFree(playerId, false)
    }

    suspend fun startPreview(playerId: UUID, vararg args: Any): PreviewResult {
        val player = Bukkit.getPlayer(playerId) ?: return PreviewResult.PLAYER_OFFLINE

        // get plot type from args
        val type = if (args.isNotEmpty() && args[0] is PlotType) {
            args[0] as PlotType
        } else {
            PlotType.PERSONAL
        }

        // Guild check
        val guildCheck = guildChecks(playerId)
        if (guildCheck != PreviewResult.SUCCESS) return guildCheck

        // Personal checks
        val personalCheck = personalChecks(playerId)
        if (personalCheck != PreviewResult.SUCCESS) return personalCheck

        val world = plotService.getPlotWorld(type).name

        val openPlot = plotLayoutService.getFirstFree(type)

        val interfaceView = menuService.previewHotbar.open(playerId)

        val plotPreview = PlotPreview(
            playerId,
            player.location.clone(),
            player.inventory.contents.clone(),
            world,
            openPlot,
            interfaceView
        )

        setPreviewPlot(playerId, plotPreview)
        toggleFree(playerId, false)
        teleportToPlot(playerId)

        return PreviewResult.SUCCESS
    }

    suspend fun cyclePreview(playerId: UUID, forward: Boolean) {
        val nextLocation = when (forward) {
            true -> {
                nextPlot(playerId)
                getPreviewPlot(playerId) ?: return
            }

            false -> {
                previousPlot(playerId)
                getPreviewPlot(playerId) ?: return
            }
        }

        teleportToPlot(playerId)
    }

    suspend fun switchPreviewType(playerId: UUID): Boolean {
        val preview = getPreview(playerId) ?: return false
        val newPlot = when (getPlotType(playerId)) {
            PlotType.PERSONAL -> {
                // Do checks for guild
                val guildCheck = guildChecks(playerId)
                if (guildCheck != PreviewResult.SUCCESS) {
                    return false
                }
                plotLayoutService.getFirstFree(PlotType.GUILD)
            }

            PlotType.GUILD -> {
                // Do checks for personal
                val personalCheck = personalChecks(playerId)
                if (personalCheck != PreviewResult.SUCCESS) {
                    return false
                }
                plotLayoutService.getFirstFree(PlotType.PERSONAL)
            }
        }
        // Checks are finished, switch plot
        toggleFree(playerId, true)
        preview.setPreviewPlot(newPlot)
        toggleFree(playerId, false)
        teleportToPlot(playerId)
        return true
    }

    suspend fun endPreview(playerId: UUID, teleport: Boolean): Boolean {
        val preview = getPreview(playerId) ?: return false

        preview.interfaceView.close() // Close interface
        if (teleport) {
            returnTeleport(playerId)
        }
        returnInventory(playerId)
        toggleFree(playerId, true)

        playerService.allowFlight(playerId, false)
        // Finally remove preview
        previews.remove(playerId)
        return true
    }

    suspend fun claimPlot(playerId: UUID): Boolean {
        val preview = getPreview(playerId) ?: return false
        val plotType = getPlotType(playerId)

        plotClaimService.createPlot(playerId, plotType, preview.previewPlot.value)

        endPreview(playerId, false)
        return true
    }
}

//    suspend fun claimPlot(player: UUID) {
//        val previewInstance = previews[player] // Get preview
//        val bukkitPlayer = player.player()
//
//        if (previewInstance == null) {
//            logger.error("No such preview")
//            return
//        }
//
//        when (previewInstance.type) {
//            PlotType.PERSONAL -> {
//                personalPreviewHandler.openPlots.remove(previewInstance.viewPlot)
//            }
//
//            PlotType.GUILD -> {
//                guildPlotLayoutStrategy.openPlots.remove(previewInstance.viewPlot)
//            }
//        }
//
//        previewInstance.interfaceView?.close()
//        previewInstance.returnInventory()
//        bukkitPlayer?.allowFlight = false
//        bukkitPlayer?.isFlying = false
//        previews.remove(player) // Remove from map
//    }


//    suspend fun nextPlot(player: UUID) {
//        val previewInstance = previews[player]
//
//        if (previewInstance == null) {
//            logger.error("No such preview")
//            return
//        }
//
//        previewInstance.viewPlot.value.free = true
//        when (previewInstance.type) { // Handle guild vs personal logic
//            PlotType.PERSONAL ->  {
//                previewInstance.viewPlot = personalPreviewHandler.getNextFreePlot(previewInstance.viewPlot) ?: run {
//                    logger.error("No free personal plots forwards :(") // Handle having no free plots
//                    return
//                }
//            }
//            PlotType.GUILD -> {
//                previewInstance.viewPlot = guildPlotLayoutStrategy.getNextFreePlot(previewInstance.viewPlot) ?: run {
//                    logger.error("No free guild plots forwards :(") // Handle having no free plots
//                    return
//                }
//            }
//        }
//
//        previewInstance.teleportToViewPlot() // Update player logic
//    }

//    suspend fun previousPlot(player: UUID) {
//        val previewInstance = previews[player]
//
//        if (previewInstance == null) {
//            logger.error("No such preview")
//            return
//        }
//
//        previewInstance.viewPlot.value.free = true // Free up current plot for next players
//        when (previewInstance.type) { // Handle guild vs personal logic
//            PlotType.PERSONAL ->  {
//                previewInstance.viewPlot = personalPreviewHandler.getPreviousFreePlot(previewInstance.viewPlot) ?: run {
//                    logger.error("No free personal plots backwards :(") // Handle having no free plots
//                    return
//                }
//            }
//            PlotType.GUILD -> {
//                previewInstance.viewPlot = guildPlotLayoutStrategy.getPreviousFreePlot(previewInstance.viewPlot) ?: run {
//                    logger.error("No free guild plots backwards :(") // Handle having no free plots
//                    return
//                }
//            }
//        }
//
//        previewInstance.teleportToViewPlot() // Update player logic
//    }