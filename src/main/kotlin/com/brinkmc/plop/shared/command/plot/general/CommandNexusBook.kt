package com.brinkmc.plop.shared.command.plot.general

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.util.cmd.CmdAddon
import com.brinkmc.plop.shared.util.message.MessageKey
import com.brinkmc.plop.shared.util.message.SoundKey
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.incendo.cloud.annotations.Command

class CommandNexusBook(override val plugin: Plop) : Addon, CmdAddon {

    @Command("nexus")
    suspend fun nexus(
        sender: CommandSourceStack
    ) {
        val player = getPlayer(sender.sender)

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

