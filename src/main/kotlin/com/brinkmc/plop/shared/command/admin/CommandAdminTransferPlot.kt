package com.brinkmc.plop.shared.command.admin

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.Command
import com.brinkmc.plop.shared.util.design.enums.MessageKey
import org.incendo.cloud.paper.util.sender.Source
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.context.CommandContext

internal class CommandAdminTransferPlot(override val plugin: Plop) : Addon, Command {

    @Command("plop plot transfer [Player]")
    suspend fun claimPlot(
        sender: CommandContext<Source>,
        @Argument("Player") receiver: Player
    ) {
        val player = getPlayer(sender.sender())

        if (!player.hasPermission("plop.admin.claim")) {
            player.sendMiniMessage(MessageKey.NO_PERMISSION)
            return
        }

    }

}


