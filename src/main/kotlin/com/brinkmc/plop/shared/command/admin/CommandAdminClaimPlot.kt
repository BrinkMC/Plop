package com.brinkmc.plop.shared.command.admin

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.util.design.enums.MessageKey
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Command

internal class CommandAdminClaimPlot(override val plugin: Plop) : Addon, Command {

    @Command("plop plot claim")
    suspend fun claimPlot(
        sender: CommandSourceStack
    ){
        val player = getPlayer(sender.sender) as Player

        if (!player.hasPermission("plop.admin.claim")) {
            messages.sendMiniMessage(player, MessageKey.NO_PERMISSION)
            return
        }

    }

}


