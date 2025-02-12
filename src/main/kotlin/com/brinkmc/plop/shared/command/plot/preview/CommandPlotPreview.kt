package com.brinkmc.plop.shared.command.plot.preview

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.shared.base.Addon
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.CommandDescription
import org.incendo.cloud.annotations.Permission
import javax.annotation.Nullable

internal class CommandPlotPreview(override val plugin: Plop) : Addon {

    @Command("preview [plotType]")
    @CommandDescription("preview a plot if not already claimed")
    @Permission("plop.plot.command.preview")
    suspend fun plotPreview(
        sender: Player,
        @Nullable type: String = "personal"
    ) {
        val plotType = PlotType.valueOf(type.lowercase().replaceFirstChar { it -> it.uppercase()})

        plots.previewHandler.startPreview(sender.uniqueId, plotType) // Initiate preview
    }


}