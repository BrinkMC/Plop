package com.brinkmc.plop.shared.command.admin

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.util.cmd.CmdAddon
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.incendo.cloud.annotations.Command

internal class CommandAdminClaimPlot(override val plugin: Plop) : Addon, CmdAddon {

    @Command("plop plot claim")
    suspend fun claimPlot(
        sender: CommandSourceStack
    ){
        val player = getPlayer(sender.sender)

        if (!player.hasPermission("plop.admin.claim")) {
            player.sendMiniMessage("no-permission")
            return
        }

    }

}


