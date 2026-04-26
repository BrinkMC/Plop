package com.brinkmc.plop.plot.controller.command.teleport

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.constant.PlotType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.constant.MessageKey
import com.brinkmc.plop.shared.constant.ServiceResult
import com.brinkmc.plop.shared.constant.SoundKey
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command

internal class CommandHome(override val plugin: Plop): Addon {

    @Command("plot home [plot_type]")
    suspend fun goHome(
        sender: CommandSourceStack,
        @Argument("plot_type") type: PlotType?
    ) {
        val playerId = sender.executor?.uniqueId ?: return // Not a player

        when (val result = plotVisitService.teleportToHome(playerId, type)) {
            is ServiceResult.Success -> {
                messages.resolveSuccess(playerId, result)
                return
            }
            is ServiceResult.Failure -> {
                messages.resolveFailure(playerId, result)
                return
            }
        }

    }
}