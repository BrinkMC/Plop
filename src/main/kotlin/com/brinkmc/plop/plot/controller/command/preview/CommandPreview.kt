package com.brinkmc.plop.plot.controller.command.preview

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.constant.PlotType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.constant.ServiceResult
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command

internal class CommandPreview(override val plugin: Plop): Addon {

    @Command("plot preview [plot_type]")
    suspend fun previewPlot(
        sender: CommandSourceStack,
        @Argument("plot_type") type: PlotType?
    ) {
        val playerId = sender.executor?.uniqueId ?: return // Not a player

        when (val result = plotPreviewService.startPreview(playerId, type)) {
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