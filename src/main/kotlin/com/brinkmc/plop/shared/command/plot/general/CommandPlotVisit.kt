package com.brinkmc.plop.shared.command.plot.general

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.Command
import com.brinkmc.plop.shared.constant.MessageKey
import com.brinkmc.plop.shared.constant.SoundKey
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.future.await
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command

class CommandPlotVisit(override val plugin: Plop) : Addon, Command {

    @Command("plot visit|visit <Player> [PlotType]")
    suspend fun visit(
        sender: CommandSourceStack,
        @Argument("Player") receiver: Player,
        @Argument("PlotType") type: PlotType?
    ) {
        val player = getPlayer(sender.sender)

        val choice = plugin.menus.selectionOtherMenu.requestChoice(player, receiver, type)

        if (choice == null) {
            player.sendMiniMessage(MessageKey.NOT_PLOT)
            player.sendSound(SoundKey.FAILURE)
            return
        }

        postTypeChosen(player, receiver, choice)
    }

    suspend fun postTypeChosen(player: Player, receiver: Player, type: PlotType) { asyncScope {
        val plot = receiver.getPlot(type)

        if (plot == null) {
            player.sendMiniMessage(MessageKey.NOT_PLOT)
            player.sendSound(SoundKey.FAILURE)
            return@asyncScope
        }

        if (plot.visit.visitable == false) {
            player.sendMiniMessage(MessageKey.NOT_VISITABLE)
            player.sendSound(SoundKey.FAILURE)
            return@asyncScope
        }

        val status = performTeleportCountdown(player)
        if (status == MessageKey.TELEPORT_INTERRUPTED) {
            player.sendSound(SoundKey.FAILURE)
            player.sendMiniMessage(MessageKey.TELEPORT_INTERRUPTED)
            return@asyncScope
        }

        val result = plot.claim.visit.let {
            player.teleportAsync(it)
        }

        if (result.await() == false) { // Ensure teleport was successful
            player.sendMiniMessage(MessageKey.TELEPORT_FAILED)
            return@asyncScope
        }

        player.sendSound(SoundKey.TELEPORT)
        player.sendMiniMessage(MessageKey.TELEPORT_COMPLETE)
    } }
}