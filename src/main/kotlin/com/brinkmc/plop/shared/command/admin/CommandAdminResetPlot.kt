package com.brinkmc.plop.shared.command.admin

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.Command
import com.brinkmc.plop.shared.constant.MessageKey
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command

internal class CommandAdminResetPlot(override val plugin: Plop) : Addon, Command {

    @Command("plop plot reset <Player> <PlotType>")
    suspend fun reset(
        sender: CommandSourceStack,
        @Argument("Player") receiver: Player,
        @Argument("PlotType") type: PlotType
    ) {
        val player = getPlayer(sender.sender)

        if (!player.hasPermission("plop.admin.claim")) {
            player.sendMiniMessage(MessageKey.NO_PERMISSION)
            return
        }

        when (type) {
            PlotType.PERSONAL -> resetPersonalPlot(player, receiver)
            PlotType.GUILD -> resetGuildPlot(player, receiver)
        }
    }

    suspend fun resetPersonalPlot(player: Player, receiver: Player) {
        val personalPlot = receiver.personalPlot()

        if (personalPlot == null) {
            player.sendMiniMessage(MessageKey.NO_PLOT)
            return
        }

        plugin.hooks.worldGuard.deleteRegion(personalPlot.plotId)
        plots.handler.deletePlot(personalPlot)
        player.sendMiniMessage(MessageKey.CLAIM_SUCCESS)
    }

    suspend fun resetGuildPlot(player: Player, receiver: Player) {
        val guildPlot = receiver.guildPlot()

        if (guildPlot == null) {
            player.sendMiniMessage(MessageKey.NO_PLOT)
            return
        }

        plugin.hooks.worldGuard.deleteRegion(guildPlot.plotId)
        plots.handler.deletePlot(guildPlot)
        player.sendMiniMessage(MessageKey.CLAIM_SUCCESS)
    }

}


