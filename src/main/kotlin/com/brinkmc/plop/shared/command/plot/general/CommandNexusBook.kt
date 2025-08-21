package com.brinkmc.plop.shared.command.plot.general

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.Command
import com.brinkmc.plop.shared.util.design.enums.MessageKey
import com.brinkmc.plop.shared.util.design.enums.SoundKey
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.incendo.cloud.annotations.Command

class CommandNexusBook(override val plugin: Plop) : Addon, Command {

    @Command("nexus")
    suspend fun nexus(
        sender: CommandSourceStack
    ) {
        val player = getPlayer(sender.sender) as org.bukkit.entity.Player

        if (player.inventory.contains(plots.nexusManager.NEXUS_BOOK)) {
            player.sendMiniMessage(MessageKey.NEXUS_BOOK_PRESENT)
            return
        }

        if (!player.world.isPlotWorld()) {
            player.sendMiniMessage(MessageKey.NOT_PLOT)
            return
        }

        if (player.getCurrentPlot()?.owner?.isPlayer(player) != true) {
            player.sendMiniMessage(MessageKey.NOT_OWNER)
            return
        }

        player.inventory.addItem(plots.nexusManager.NEXUS_BOOK)
        player.sendMiniMessage(MessageKey.NEXUS_BOOK_GIVEN)
        player.sendSound(SoundKey.RECEIVE_ITEM)
    }
}

