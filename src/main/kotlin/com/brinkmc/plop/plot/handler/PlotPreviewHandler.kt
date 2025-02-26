package com.brinkmc.plop.plot.handler

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.layout.GuildPlotLayoutStrategy
import com.brinkmc.plop.plot.layout.PersonalPlotLayoutStrategy
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.plot.preview.PreviewInstance
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import java.util.UUID

/*
Keep track of all active preview instances
Ensure that data is saved e.t.c
 */

class PlotPreviewHandler(override val plugin: Plop): Addon, State {

    lateinit var guildPlotLayoutStrategy: GuildPlotLayoutStrategy
    lateinit var personalPreviewHandler: PersonalPlotLayoutStrategy

    private val previews = mutableMapOf<UUID, PreviewInstance>()

    override suspend fun load() {
        guildPlotLayoutStrategy = GuildPlotLayoutStrategy(plugin, PlotType.GUILD)
        guildPlotLayoutStrategy.load()
        personalPreviewHandler = PersonalPlotLayoutStrategy(plugin, PlotType.PERSONAL)
        personalPreviewHandler.load()
    }

    override suspend fun kill() {
        for (preview in previews) {
            preview.value.returnTeleport()
            preview.value.interfaceView?.close()
            preview.value.returnInventory()
        }

        previews.clear()
    }

    suspend fun startPreview(player: UUID, type: PlotType) {

        val bukkitPlayer = player.player()

        if (bukkitPlayer == null) { // Validation check to see if player exists
            logger.error("Failed to start preview, player doesn't exist")
            return
        }

        val guild = bukkitPlayer.guild()

        // Handle potential forced guild plot errors
        if (type == PlotType.GUILD && guild == null) {
            bukkitPlayer.sendMiniMessage("preview.start.no-guild")
            return
        }

        if (type == PlotType.GUILD && (guild?.size ?: 0) <= plotConfig.guildConfig.minSize-1) {
            bukkitPlayer.sendMiniMessage("preview.start.guild-too-small")
            return
        }

        if (type == PlotType.GUILD && guild?.guildMaster?.uuid != player) {
            bukkitPlayer.sendMiniMessage("preview.start.not-guild-master")
            return
        }

        // Handle if player is trying to preview with a plot, match all possible combinations
        val guildPlot = bukkitPlayer.guildPlot()
        val personalPlot = bukkitPlayer.personalPlot()

        if (guildPlot != null && personalPlot != null) {
            bukkitPlayer.sendMiniMessage("preview.plots.max-plots")
            return
        }

        if (personalPlot != null && type == PlotType.PERSONAL) {
            bukkitPlayer.sendMiniMessage("preview.has-plots.personal")
            return
        }

        if (guildPlot != null && type == PlotType.GUILD) {
            bukkitPlayer.sendMiniMessage("preview.has-plots.guild")
            return
        }

        val viewPlot = when (type) {
            PlotType.PERSONAL -> {
                personalPreviewHandler.getFirstFree() ?: run {
                    logger.error("No free personal plots :(") // Handle having no free plots
                    return
                }
            }

            PlotType.GUILD -> {
                guildPlotLayoutStrategy.getFirstFree() ?: run {
                    logger.error("No free guild plots :(") // Handle having no free plots
                    return
                }
            }
        }

        val previewInstance = PreviewInstance( // Create new preview instance
            plugin,
            type,
            viewPlot,
            player,
            bukkitPlayer.location.clone(),
            bukkitPlayer.inventory.contents.clone() // Save inventory or nothing
        )

        // Add to the loaded instances
        previews.put(player, previewInstance)

        previewInstance.teleportToViewPlot() // Update player to correct location
        val view = plugin.menus.hotbarPreview.open(bukkitPlayer, null) // Start hotbar preview
        previewInstance.interfaceView = view // Save hotbar preview to instance
        bukkitPlayer.updateBorder() // Update border
    }

    suspend fun switchPreview(player: UUID) {
        val previewInstance = previews[player] // Get preview

        if (previewInstance == null) {
            logger.error("No such preview")
            return
        }

        val bukkitPlayer = player.player()

        if (bukkitPlayer == null) { // Validation check to see if player exists
            logger.error("Failed to switch preview, player doesn't exist")
            return
        }

        val guild = bukkitPlayer.guild()

        if (previewInstance.type == PlotType.PERSONAL && guild == null) {
            bukkitPlayer.sendMiniMessage("preview.switch.no-guild")
            return
        }

        if (previewInstance.type == PlotType.PERSONAL && (guild?.size ?: 0) <= plotConfig.guildConfig.minSize-1) {
            bukkitPlayer.sendMiniMessage("preview.switch.guild-too-small")
            return
        }

        if (previewInstance.type == PlotType.PERSONAL && guild?.guildMaster?.uuid != player) {
            bukkitPlayer.sendMiniMessage("preview.switch.not-guild-master")
            return
        }

        if (previewInstance.type == PlotType.PERSONAL && bukkitPlayer.guildPlot() != null) {
            bukkitPlayer.sendMiniMessage("preview.switch.has-guild-plot")
            return
        }

        if (previewInstance.type == PlotType.GUILD && bukkitPlayer.personalPlot() != null) {
            bukkitPlayer.sendMiniMessage("preview.switch.has-personal-plot")
            return
        }

        previewInstance.type = when (previewInstance.type) { // Invert previews
            PlotType.PERSONAL -> PlotType.GUILD
            PlotType.GUILD -> PlotType.PERSONAL
        }

        when (previewInstance.type) {
            PlotType.PERSONAL -> {
                previewInstance.viewPlot = personalPreviewHandler.getFirstFree() ?: run {
                    logger.error("No free personal plots :(") // Handle having no free plots
                    return
                }
            }

            PlotType.GUILD -> {
                previewInstance.viewPlot = guildPlotLayoutStrategy.getFirstFree() ?: run {
                    logger.error("No free guild plots :(") // Handle having no free plots
                    return
                }
            }
        }

        previewInstance.teleportToViewPlot() // Teleport
        bukkitPlayer.updateBorder() // Update border
    }

    suspend fun endPreview(player: UUID) {
        val previewInstance = previews[player] // Get preview

        if (previewInstance == null) {
            logger.error("No such preview")
            return
        }


        previewInstance.returnTeleport()
        previewInstance.interfaceView?.close()
        previewInstance.returnInventory()

        previews.remove(player) // Remove from map
    }

    suspend fun claimPlot(player: UUID) {
        val previewInstance = previews[player] // Get preview
        val bukkitPlayer = player.player()

        if (previewInstance == null) {
            logger.error("No such preview")
            return
        }

        when(previewInstance.type) {
            PlotType.PERSONAL -> {
                personalPreviewHandler.openPlots.remove(previewInstance.viewPlot)
            }
            PlotType.GUILD -> {
                guildPlotLayoutStrategy.openPlots.remove(previewInstance.viewPlot)
            }
        }

        previewInstance.interfaceView?.close()
        previewInstance.returnInventory()
        bukkitPlayer?.allowFlight = false
        bukkitPlayer?.isFlying = false
        previews.remove(player) // Remove from map
    }

    suspend fun nextPlot(player: UUID) {
        val previewInstance = previews[player]

        if (previewInstance == null) {
            logger.error("No such preview")
            return
        }

        previewInstance.viewPlot.value.free = true
        when (previewInstance.type) { // Handle guild vs personal logic
            PlotType.PERSONAL ->  {
                previewInstance.viewPlot = personalPreviewHandler.getNextFreePlot(previewInstance.viewPlot) ?: run {
                    logger.error("No free personal plots forwards :(") // Handle having no free plots
                    return
                }
            }
            PlotType.GUILD -> {
                previewInstance.viewPlot = guildPlotLayoutStrategy.getNextFreePlot(previewInstance.viewPlot) ?: run {
                    logger.error("No free guild plots forwards :(") // Handle having no free plots
                    return
                }
            }
        }

        previewInstance.teleportToViewPlot() // Update player logic
    }

    suspend fun previousPlot(player: UUID) {
        val previewInstance = previews[player]

        if (previewInstance == null) {
            logger.error("No such preview")
            return
        }

        previewInstance.viewPlot.value.free = true // Free up current plot for next players
        when (previewInstance.type) { // Handle guild vs personal logic
            PlotType.PERSONAL ->  {
                previewInstance.viewPlot = personalPreviewHandler.getPreviousFreePlot(previewInstance.viewPlot) ?: run {
                    logger.error("No free personal plots backwards :(") // Handle having no free plots
                    return
                }
            }
            PlotType.GUILD -> {
                previewInstance.viewPlot = guildPlotLayoutStrategy.getPreviousFreePlot(previewInstance.viewPlot) ?: run {
                    logger.error("No free guild plots backwards :(") // Handle having no free plots
                    return
                }
            }
        }

        previewInstance.teleportToViewPlot() // Update player logic
    }

    fun getPreview(player: UUID): PreviewInstance? {
        return previews[player]
    }
}