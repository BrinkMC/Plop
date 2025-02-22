package com.brinkmc.plop.shared.command.plot.general

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.util.CmdAddon
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command

class CommandPlotHome(override val plugin: Plop) : Addon, CmdAddon {

    @Command("plot home|home [PlotType]")
    suspend fun goHome(
        sender: CommandSourceStack,
        @Argument("PlotType") type: PlotType?
    ) {
        val player = getPlayer(sender.sender)

        val choice = plugin.menus.selectionSelfMenu.requestChoice(player, type, null) // Get choice because none was specified originally

        if (choice == null) {
            player.sendMiniMessage(lang.get("plot.no-plot"))
            return
        }

        postTypeChosen(player, choice)
    }

    suspend fun postTypeChosen(player: Player, type: PlotType) {
        val plot = player.getPlot(type)
        syncScope {
            plot?.claim?.home?.let { player.teleport(it) }
        }
        player.sendMiniMessage(lang.get("plot.teleport-complete"))
    }
}