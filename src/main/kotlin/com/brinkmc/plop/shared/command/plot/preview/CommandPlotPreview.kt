package com.brinkmc.plop.shared.command.plot.preview

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.command.utils.CmdAddon
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.command.CommandSender
import org.incendo.cloud.paper.util.sender.Source
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.CommandDescription
import org.incendo.cloud.annotations.Permission
import org.incendo.cloud.paper.util.sender.PlayerSource
import javax.annotation.Nullable

internal class CommandPlotPreview(override val plugin: Plop): Addon, CmdAddon {

    @Command("plot preview [PlotType]")
    @CommandDescription("preview a plot if not already claimed")
    @Permission("plop.plot.command.preview")
    suspend fun plotPreview(
        sender: CommandSourceStack,
        @Nullable @Argument("PlotType") type: PlotType?
    ) {
        val player = getPlayer(sender.sender)

        if (type != null) {
            plots.previewHandler.startPreview(player.uniqueId, type) // Initiate preview
            return
        }

        val personalPlot = player.personalPlot()
        val guildPlot = player.guildPlot()

        if (personalPlot == null && guildPlot == null) {
            plots.previewHandler.startPreview(player.uniqueId, PlotType.PERSONAL) // Initiate preview personal if left empty
            return
        }

        if (personalPlot != null && guildPlot == null) {
            plots.previewHandler.startPreview(player.uniqueId, PlotType.GUILD) // Initiate preview guild
            return
        }

        if (personalPlot == null) {
            plots.previewHandler.startPreview(player.uniqueId, PlotType.PERSONAL) // Initiate preview personal
            return
        }

        player.sendFormattedMessage(lang.get("command.already-claimed"))
    }


}