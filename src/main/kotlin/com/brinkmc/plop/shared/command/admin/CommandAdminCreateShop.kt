package com.brinkmc.plop.shared.command.admin

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.Command
import com.brinkmc.plop.shared.design.enums.MessageKey
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.incendo.cloud.annotations.Command

internal class CommandAdminCreateShop(override val plugin: Plop) : Addon, Command {

    @Command("plop claim plot")
    suspend fun claimPlot(
        sender: CommandSourceStack
    ) {
        val player = getPlayer(sender.sender)
        if (!player.hasPermission("plop.admin.claim")) {
            player.sendMiniMessage(MessageKey.NO_PERMISSION)
            return
        }

    }
}