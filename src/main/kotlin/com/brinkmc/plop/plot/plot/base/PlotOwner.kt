package com.brinkmc.plop.plot.plot.base

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.UUID

sealed class PlotOwner {
    data class GuildOwner(val guild: Guild, private val members: MutableList<UUID>): PlotOwner() {
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