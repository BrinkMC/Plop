package com.brinkmc.plop.plot.controller.command.nexus

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.constant.ServiceResult
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.incendo.cloud.annotations.Command

internal class CommandNexusBook(override val plugin: Plop): Addon {

    @Command("nexus book")
    suspend fun nexusBook(
        sender: CommandSourceStack
    ) {
        val playerId = sender.executor?.uniqueId ?: return // Not a player

        when (val result = plotNexusService.giveNexusBook(playerId, sender.location, true)) {
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