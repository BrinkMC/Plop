package com.brinkmc.plop.shared.command.admin

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.command.utils.CmdAddon
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.command.CommandSender
import org.incendo.cloud.paper.util.sender.Source
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.paper.util.sender.PlayerSource

internal class CommandAdminUnclaimPlot(override val plugin: Plop) : Addon, CmdAddon {

    @Command("plop plot unclaim")
    suspend fun claimPlot(
        sender: CommandSourceStack
    ) {
        val player = getPlayer(sender.sender)

        if (!player.hasPermission("plop.admin.claim")) {
            player.sendFormattedMessage(lang.get("no-permission"))
            return
        }

    }

}


