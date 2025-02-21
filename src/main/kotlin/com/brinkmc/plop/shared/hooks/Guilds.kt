package com.brinkmc.plop.shared.hooks

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.api.GuildsAPI
import me.glaremasters.guilds.api.events.GuildCreateEvent
import me.glaremasters.guilds.guild.Guild
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import java.util.UUID

class Guilds(override val plugin: Plop): Addon, State {

    private lateinit var guildAPI: GuildsAPI // Guild API

    override suspend fun load() {
        guildAPI = Guilds.getApi()
    }

    override suspend fun kill() {
        TODO("Not yet implemented")
    }

    fun getGuild(uuid: UUID): Guild? {
        return guildAPI.getGuild(Bukkit.getOfflinePlayer(uuid))
    }

    fun getGuildFromPlayer(uuid: UUID): Guild? {
        return guildAPI.getGuildByPlayerId(uuid)
    }
}