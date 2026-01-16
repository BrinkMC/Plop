package com.brinkmc.plop.plot.controller.command.teleport

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.constant.PlotType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.constant.MessageKey
import com.brinkmc.plop.shared.constant.SoundKey
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command

internal class CommandHome(override val plugin: Plop): Addon {

    @Command("plot home [plot_type]")
    suspend fun setHome(
        sender: CommandSourceStack,
        @Argument("plot_type") type: PlotType?
    ) {
        val playerId = sender.executor?.uniqueId ?: return // Not a player

        // If type is provided, use it directly
        if (type != null) {

        }
        // Code before
        val choice = menuService.plotTypeMenu.request(playerId, null, playerId)
        // Code after
        if (choice == null) { // Check if choice is null
            player.sendMiniMessage(MessageKey.NOT_PLOT)
            player.sendSound(SoundKey.FAILURE)
            return
        }

        postTypeChosen(player, choice)

    }
}