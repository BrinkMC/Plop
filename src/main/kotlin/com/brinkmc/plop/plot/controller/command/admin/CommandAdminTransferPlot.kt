package com.brinkmc.plop.plot.controller.command.admin

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.incendo.cloud.annotations.Command

internal class CommandAdminTransferPlot(override val plugin: Plop): Addon {

    @Command("plop plot claim <player_list >")
    suspend fun setHome(
        sender: CommandSourceStack
    ) {
        val playerId = sender.executor?.uniqueId ?: return // Not a player

        TODO()
    }
}