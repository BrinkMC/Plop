package com.brinkmc.plop.shared.command.plot.preview

import com.brinkmc.plop.Plop
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
        @Nullable plotType: String = "personal"
    ) {
        plotType.valueOf(plotType.lowercase().replaceFirstChar { it -> it.uppercase()})
    }


}