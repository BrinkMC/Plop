package com.brinkmc.plop.plot.plot.base

import me.glaremasters.guilds.guild.Guild
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.UUID

sealed class PlotOwner {
    data class GuildOwner(val guild: Guild?): PlotOwner() {

        val members: MutableList<UUID> = guild?.members?.map { it.uuid }?.toMutableList() ?: mutableListOf()

        fun getMembers(): List<OfflinePlayer> {
            return members.map { uuid -> Bukkit.getOfflinePlayer(uuid) }
        }

        fun addMember(newMember: UUID) {
            members.add(newMember)
        }

        fun removeMember(oldMember: UUID) {
            members.remove(oldMember)
        }
    }


    data class PlayerOwner(val player: OfflinePlayer): PlotOwner() {}
}