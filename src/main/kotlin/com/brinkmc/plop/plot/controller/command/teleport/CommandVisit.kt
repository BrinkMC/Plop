package com.brinkmc.plop.plot.controller.command.teleport

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.constant.PlotType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.constant.ServiceResult
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import java.util.UUID

internal class CommandVisit(override val plugin: Plop): Addon {

    @Command("plot visit <player> [plot_type]")
    suspend fun visit(
        sender: CommandSourceStack,
        @Argument("player") targetId: UUID,
        @Argument("plot_type") type: PlotType?
    ) {
        val playerId = sender.executor?.uniqueId ?: return // Not a player

        /*TODO
            Big overhaul to include the ranking system of plots

         */
        when (val result = plotVisitService.visitPlot(playerId, targetId, type)) {
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