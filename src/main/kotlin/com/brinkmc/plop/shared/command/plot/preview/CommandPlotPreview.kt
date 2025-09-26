package com.brinkmc.plop.shared.command.plot.preview

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.Command
import com.brinkmc.plop.shared.constant.MessageKey
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.CommandDescription
import org.incendo.cloud.annotations.Permission

internal class CommandPlotPreview(override val plugin: Plop): Addon, Command {

    @Command("plot preview [PlotType]")
    @CommandDescription("preview a plot if not already claimed")
    @Permission("plop.plot.command.preview")
    suspend fun plotPreview(
        sender: CommandSourceStack,
        @Argument("PlotType") type: PlotType?
    ) {
        val player = getPlayer(sender.sender)

        val personalPlot = player.personalPlot() // Get potential plots
        val guildPlot = player.guildPlot()

        if (plots.previewHandler.getPreview(player.uniqueId) != null) {
            player.sendMiniMessage(MessageKey.PREVIEW_IN_PROGRESS)
            return
        }

        asyncScope {
            if (personalPlot == null && guildPlot == null) {
                plots.previewHandler.startPreview(player.uniqueId, PlotType.PERSONAL) // Initiate preview personal if left empty
                return@asyncScope
            }

            if (personalPlot != null && guildPlot == null) {
                plots.previewHandler.startPreview(player.uniqueId, PlotType.GUILD) // Initiate preview guild
                return@asyncScope
            }

            if (personalPlot == null) {
                plots.previewHandler.startPreview(player.uniqueId, PlotType.PERSONAL) // Initiate preview personal
                return@asyncScope
            }

            player.sendMiniMessage(MessageKey.MAX_PLOTS_REACHED)
        }
    }
}