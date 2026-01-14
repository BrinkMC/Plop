package com.brinkmc.plop.plot.controller.listener

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.constant.MessageKey
import com.brinkmc.plop.shared.constant.ServiceResult
import com.brinkmc.plop.shared.constant.SoundKey
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerCommandSendEvent
import org.bukkit.event.player.PlayerJoinEvent

class CommandListener(override val plugin: Plop): Addon, State, Listener  {

    override suspend fun load() {
        TODO("Not yet implemented")
    }

    override suspend fun kill() {
        TODO("Not yet implemented")
    }

    @EventHandler(priority = EventPriority.LOWEST)
    suspend fun commandSendEvent(event: PlayerCommandPreprocessEvent) {
        val player = event.player
        event.isCancelled = true
        // If player is in preview, cancel the command
        if (plotPreviewService.isInPreview(player.uniqueId)) {
            event.isCancelled = false
            return
        }

        val failure = ServiceResult.Failure(MessageKey.PREVIEW_NO_COMMANDS, SoundKey.FAILURE)
        messages.resolveFailure(player.uniqueId, failure)
    }
}