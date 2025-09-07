package com.brinkmc.plop.plot.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.constant.PlotType
import com.brinkmc.plop.plot.constant.PreviewResult
import com.brinkmc.plop.plot.dto.PlotPreview
import com.brinkmc.plop.plot.layout.GuildPlotLayoutStrategy
import com.brinkmc.plop.plot.layout.PersonalPlotLayoutStrategy
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.plot.preview.PreviewInstance
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.design.enums.MessageKey
import org.bukkit.Bukkit
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
            preview.value.returnTeleport()
            preview.value.interfaceView?.close()
            preview.value.returnInventory()
        }

        previews.clear()
    }

    suspend fun startPreview(playerId: UUID, vararg args: Any): PreviewResult {
        val player = Bukkit.getPlayer(playerId) ?: return PreviewResult.PLAYER_OFFLINE

        val guild = hookService.guilds.getGuildFromPlayer(playerId)

        // get plot type from args
        val type = if (args.isNotEmpty() && args[0] is PlotType) {
            args[0] as PlotType
        } else {
            PlotType.PERSONAL
        }

        val guildMaster = guild?.guildMaster?.uuid
        val guildSize = guild?.size ?: 0
        val minGuildSize = configService.plotConfig.guildConfig.minSize

        // Guild checks
        if (type == PlotType.GUILD && plotService.hasGuildPlot(playerId)) return PreviewResult.GUILD_HAS_PLOT
        if (type == PlotType.GUILD && guild == null) return PreviewResult.NO_GUILD
        if (type == PlotType.GUILD && guildMaster == playerId) return PreviewResult.NOT_GUILD_MASTER
        if (type == PlotType.GUILD && guildSize < minGuildSize) return PreviewResult.GUILD_TOO_SMALL

        // Personal checks
        if (type == PlotType.PERSONAL && plotService.hasPersonalPlot(playerId)) return PreviewResult.PLAYER_HAS_PLOT
        val world = plotService.getPlotWorld(type).name

        val openPlot = plotLayoutService.getFirstFree(type)

        val interfaceView = menuService.previewHotbar.open(player)

        val plotPreview = PlotPreview(
            playerId,
            player.location.clone(),
            player.inventory.contents.clone(),
            world,
            openPlot,
            interfaceView
        )

        teleportToPlot(playerId)

        return PreviewResult.SUCCESS
    }

    fun teleportToPlot(id: UUID) {
        val preview = previews[id] ?: return
        val bukkitPlayer = id.player() ?: return

        bukkitPlayer.teleport(preview.viewPlot.value.centre.clone().add(0.0, 1.0, 0.0))
        preview.viewPlot.value.free = false
    }

    fun getPlotType(id: UUID): PlotType {
        if (previews[id]?.world == configService.plotConfig.personalConfig.personalPlotWorld) {
            return PlotType.PERSONAL
        } else {
            return PlotType.GUILD
        }
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
            bukkitPlayer.sendMiniMessage(MessageKey.NO_GUILD)
            return
        }

        if (type == PlotType.GUILD && (guild?.size ?: 0) <= plotConfig.guildConfig.minSize-1) {
            bukkitPlayer.sendMiniMessage(MessageKey.GUILD_TOO_SMALL)
            return
        }

        if (type == PlotType.GUILD && guild?.guildMaster?.uuid != player) {
            bukkitPlayer.sendMiniMessage(MessageKey.NOT_GUILD_MASTER)
            return
        }

        // Handle if player is trying to preview with a plot, match all possible combinations
        val guildPlot = bukkitPlayer.guildPlot()
        val personalPlot = bukkitPlayer.personalPlot()

        if (guildPlot != null && personalPlot != null) {
            bukkitPlayer.sendMiniMessage(MessageKey.MAX_PLOTS_REACHED)
            return
        }

        if (personalPlot != null && type == PlotType.PERSONAL) {
            bukkitPlayer.sendMiniMessage(MessageKey.HAS_PLOTS_PERSONAL)
            return
        }

        if (guildPlot != null && type == PlotType.GUILD) {
            bukkitPlayer.sendMiniMessage(MessageKey.HAS_PLOTS_GUILD)
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
        val view = plugin.menus.hotbarPreview.open(bukkitPlayer) // Start hotbar preview
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
            bukkitPlayer.sendMiniMessage(MessageKey.NO_GUILD)
            return
        }

        if (previewInstance.type == PlotType.PERSONAL && (guild?.size ?: 0) <= plotConfig.guildConfig.minSize-1) {
            bukkitPlayer.sendMiniMessage(MessageKey.GUILD_TOO_SMALL)
            return
        }

        if (previewInstance.type == PlotType.PERSONAL && guild?.guildMaster?.uuid != player) {
            bukkitPlayer.sendMiniMessage(MessageKey.NOT_GUILD_MASTER)
            return
        }

        if (previewInstance.type == PlotType.PERSONAL && bukkitPlayer.guildPlot() != null) {
            bukkitPlayer.sendMiniMessage(MessageKey.HAS_PLOTS_GUILD)
            return
        }

        if (previewInstance.type == PlotType.GUILD && bukkitPlayer.personalPlot() != null) {
            bukkitPlayer.sendMiniMessage(MessageKey.HAS_PLOTS_PERSONAL)
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