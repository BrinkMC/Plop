package com.brinkmc.plop.plot.plot.base

import com.brinkmc.plop.shared.hooks.Economy
import com.destroystokyo.paper.profile.PlayerProfile
import me.glaremasters.guilds.guild.Guild
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.SkullMeta
import java.util.UUID

sealed class PlotOwner {
    data class GuildOwner(val guild: Guild): PlotOwner() {

        val members: MutableList<UUID> = guild.members.map { it.uuid }.toMutableList()

        fun addMember(newMember: UUID) {
            members.add(newMember)
        }

        fun removeMember(oldMember: UUID) {
            members.remove(oldMember)
        }
    }

    data class PlayerOwner(val player: OfflinePlayer): PlotOwner() {
        fun onlinePlayer(): Player? {
            return Bukkit.getPlayer(player.uniqueId)
        }
    }

    fun getLeader(): OfflinePlayer {
        return when (this) {
            is GuildOwner -> {
                Bukkit.getOfflinePlayer(guild.guildMaster.uuid)
            }
            is PlayerOwner -> {
                Bukkit.getOfflinePlayer(player.uniqueId)
            }
        }
    }

    fun getBalance(economy: Economy): Double {
        return when (this) {
            is GuildOwner -> {
                guild.balance
            }
            is PlayerOwner -> {
                economy.getBalance(player)
            }
        }
    }

    fun hasBalance(economy: Economy, amount: Double): Boolean {
        return when (this) {
            is GuildOwner -> {
                guild.balance >= amount
            }
            is PlayerOwner -> {
                economy.hasBalance(player, amount)
            }
        }
    }

    fun withdrawBalance(economy: Economy, amount: Double): Boolean {
        return when (this) {
            is GuildOwner -> {
                guild.balance -= amount
                true
            }
            is PlayerOwner -> {
                economy.withdraw(player, amount)
            }
        }
    }

    fun depositBalance(economy: Economy, amount: Double): Boolean {
        return when (this) {
            is GuildOwner -> {
                guild.balance += amount
                true
            }
            is PlayerOwner -> {
                economy.deposit(player, amount)
            }
        }
    }

    fun isPlayer(check: Player) {
        when (this) {
            is GuildOwner -> {
                members.contains(check.uniqueId)
            }
            is PlayerOwner -> {
                player.uniqueId == check.uniqueId
            }
        }
    }

    fun getPlayers(): List<UUID> {
        return when (this) {
            is GuildOwner -> {
                members
            }
            is PlayerOwner -> {
                listOf(player.uniqueId)
            }
        }
    }

    fun getSkull(): PlayerProfile? {
        return when (this) {
            is GuildOwner -> {
                (guild.guildSkull.itemStack.itemMeta as SkullMeta).playerProfile
            }
            is PlayerOwner -> {
                player.playerProfile as PlayerProfile
            }
        }
    }
}