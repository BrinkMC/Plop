package com.brinkmc.plop.plot.controller.command.admin

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.constant.MessageKey
import com.brinkmc.plop.shared.constant.ServiceResult
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.incendo.cloud.annotations.Command

internal class CommandAdminClaimPlot(override val plugin: Plop): Addon {

    @Command("plop plot claim <player>")
    suspend fun setHome(
        sender: CommandSourceStack
    ) {
        val playerId = sender.executor?.uniqueId ?: return // Not a player

        TODO()
    }
}