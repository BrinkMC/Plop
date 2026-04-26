package com.brinkmc.plop.plot.controller.command.admin

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.constant.PlotType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.constant.MessageKey
import com.brinkmc.plop.shared.constant.ServiceResult
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import java.util.UUID

internal class CommandAdminUnclaimPlot(override val plugin: Plop): Addon {

    @Command("plop plot claim <player> [plot_type]")
    suspend fun setHome(
        sender: CommandSourceStack,
        @Argument("player") targetId: UUID,
        @Argument("plot_type") plotType: PlotType?
    ) {
        val playerId = sender.executor?.uniqueId ?: return // Not a player

        when (val result = plotClaimService.deletePlotAdmin(targetId, targetId, playerService.getLocation(playerId), plotType)) {
            is ServiceResult.Success -> {
                messages.resolveSuccess(playerId, result)
            }
            is ServiceResult.Failure -> {
                messages.resolveFailure(playerId, result)
            }
        }
    }
}