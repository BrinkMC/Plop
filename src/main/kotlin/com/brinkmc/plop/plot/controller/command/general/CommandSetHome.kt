package com.brinkmc.plop.plot.controller.command.general

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.constant.ServiceResult
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.incendo.cloud.annotations.Command

internal class CommandSetHome(override val plugin: Plop): Addon {

    @Command("plot set home")
    suspend fun setHome(
        sender: CommandSourceStack
    ) {
        val playerId = sender.executor?.uniqueId ?: return // Not a player

        when (val result = plotClaimService.setPlotHome(playerId, sender.location)) {
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