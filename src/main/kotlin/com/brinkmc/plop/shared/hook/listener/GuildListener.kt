package com.brinkmc.plop.shared.hook.listener

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import me.glaremasters.guilds.api.events.GuildJoinEvent
import me.glaremasters.guilds.api.events.GuildKickEvent
import me.glaremasters.guilds.api.events.GuildLeaveEvent
import me.glaremasters.guilds.api.events.GuildRemoveEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class GuildListener(override val plugin: Plop): Addon, State, Listener {
    override suspend fun load() {}

    override suspend fun kill() {}

    @EventHandler
    suspend fun onGuildRemove(guildRemoveEvent: GuildRemoveEvent) {
        val plot = guildRemoveEvent.guild.plot() ?: return

        plots.handler.deletePlot(plot)
    }

    @EventHandler
    suspend fun onGuildMemberAdd(event: GuildJoinEvent) {
        val plot = event.guild.plot() ?: return

        plugin.hooks.worldGuard.addMember(plot, event.player)
    }

    @EventHandler
    suspend fun onGuildMemberRemove(event: GuildLeaveEvent) {
        val plot = event.guild.plot() ?: return

        plugin.hooks.worldGuard.removeMember(plot, event.player)
    }

    @EventHandler
    suspend fun onGuildMemberRemove(event: GuildKickEvent) {
        val plot = event.guild.plot()

        if (plot == null) {
            return
        }

        plugin.hooks.worldGuard.removeMember(plot, event.kicked)
    }
}