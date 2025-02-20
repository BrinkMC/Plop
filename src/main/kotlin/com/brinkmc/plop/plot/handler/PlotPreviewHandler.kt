package com.brinkmc.plop.plot.handler

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.layout.GuildPlotLayoutStrategy
import com.brinkmc.plop.plot.layout.PersonalPlotLayoutStrategy
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.plot.preview.PreviewInstance
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.util.GuiUtils.stacksToBase64
import com.noxcrew.interfaces.InterfacesListeners
import org.bukkit.Bukkit
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
        previews.clear()
    }

    suspend fun startPreview(player: UUID, type: PlotType) {

        val bukkitPlayer = Bukkit.getPlayer(player)

        if (bukkitPlayer == null) { // Validation check to see if player exists
            logger.error("Failed to start preview, player doesn't exist")
            return
        }

        // Handle if player is trying to preview with a plot, match all possible combinations
        if (plots.handler.hasGuildPlot(player) && plots.handler.hasPersonalPlot(player)) {
            bukkitPlayer.sendFormattedMessage(lang.get("preview.has-plots.all"))
            return
        }

        if (plots.handler.hasPersonalPlot(player) && type == PlotType.PERSONAL) {
            bukkitPlayer.sendFormattedMessage(lang.get("preview.has-plots.personal"))
            return
        }

        if (plots.handler.hasGuildPlot(player) && type == PlotType.GUILD) {
            bukkitPlayer.sendFormattedMessage(lang.get("preview.has-plots.guild"))
            return
        }

        val view = plugin.menus.openHotbarPreview(bukkitPlayer)

        val previewInstance = PreviewInstance( // Create new preview instance
            plugin,
            player,
            bukkitPlayer.location.clone(),
            view
        )

        // Initiate the next two lateinit vals
        previewInstance.type = type

        when (type) {
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

        previewInstance.viewPlot.value.free = false
        bukkitPlayer.teleport(previewInstance.viewPlot.value.toLocation()) // Teleport player

        // Add to the loaded instances
        previews.put(player, previewInstance)


    }

    suspend fun switchPreview(player: UUID) {
        val previewInstance = previews[player] // Get preview

        if (previewInstance == null) {
            logger.error("No such preview")
            return
        }

        val bukkitPlayer = Bukkit.getPlayer(player)

        if (bukkitPlayer == null) { // Validation check to see if player exists
            logger.error("Failed to switch preview, player doesn't exist")
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

        bukkitPlayer.teleport(previewInstance.viewPlot.value.toLocation()) // Teleport player

        // Teleport
    }

    suspend fun endPreview(player: UUID) {
        val previewInstance = previews[player] // Get preview

        if (previewInstance == null) {
            logger.error("No such preview")
            return
        }

        previewInstance.viewPlot.value.free = true

        previewInstance.returnTeleport()

        previews.remove(player) // Remove from map

        InterfacesListeners.INSTANCE.getOpenInterface(player)?.close()

        return
    }

    suspend fun claimPlot(player: UUID) {
        val previewInstance = previews[player] // Get preview

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

        InterfacesListeners.INSTANCE.getOpenInterface(player)?.close()

        previews.remove(player) // Remove from map
    }

    fun nextPlot(player: UUID) {
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
        previewInstance.viewPlot.value.free = false

        previewInstance.teleportToViewPlot() // Update player logic
        return
    }

    fun previousPlot(player: UUID) {
        val previewInstance = previews[player]

        if (previewInstance == null) {
            logger.error("No such preview")
            return
        }

        previewInstance.viewPlot.value.free = true
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
        previewInstance.viewPlot.value.free = false

        previewInstance.teleportToViewPlot() // Update player logic
        return
    }

    fun getPreview(player: UUID): PreviewInstance? {
        return previews[player]
    }
}