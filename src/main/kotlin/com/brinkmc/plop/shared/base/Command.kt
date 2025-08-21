package com.brinkmc.plop.shared.base

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.incendo.cloud.paper.util.sender.Source

internal interface Command {

    fun getPlayer(sender: Source): Player {
        return sender.source() as Player
    }

    fun getPlayer(sender: CommandSender): Player {
        return sender as Player
    }
}