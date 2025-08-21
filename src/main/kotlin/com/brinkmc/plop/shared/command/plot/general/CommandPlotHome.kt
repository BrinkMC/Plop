package com.brinkmc.plop.shared.command.plot.general

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.Command
import com.brinkmc.plop.shared.util.design.enums.MessageKey
import com.brinkmc.plop.shared.util.design.enums.SoundKey
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.future.await
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command

class CommandPlotHome(override val plugin: Plop) : Addon, Command {

    @Command("plot home|home [PlotType]")
    suspend fun goHome(
        sender: CommandSourceStack,
        @Argument("PlotType") type: PlotType?
    ) {
        val player = getPlayer(sender.sender)

        val choice = plugin.menus.selectionSelfMenu.requestChoice(player, type) // Get choice because none was specified originally

        if (choice == null) { // Check if choice is null
            player.sendMiniMessage(MessageKey.NOT_PLOT)
            player.sendSound(SoundKey.FAILURE)
            return
        }

        postTypeChosen(player, choice)
    }

    suspend fun postTypeChosen(player: Player, type: PlotType) { asyncScope {
        val plot = player.getPlot(type)

        if (plot == null) {
            player.sendMiniMessage(MessageKey.NOT_PLOT)
            player.sendSound(SoundKey.FAILURE)
            return@asyncScope
        }

        val status = performTeleportCountdown(player)
        if (status == MessageKey.TELEPORT_INTERRUPTED) {
            player.sendSound(SoundKey.FAILURE)
            player.sendMiniMessage(MessageKey.TELEPORT_INTERRUPTED)
            return@asyncScope
        }

        val result = plot.claim.home.let {
            player.teleportAsync(it)
        }

        if (result.await() == false) {
            player.sendMiniMessage(MessageKey.TELEPORT_FAILED)
            return@asyncScope
        }

        player.sendSound(SoundKey.TELEPORT)
        player.sendMiniMessage(MessageKey.TELEPORT_COMPLETE)
    } }
}