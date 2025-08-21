package com.brinkmc.plop.shared.hook.api

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.plugin.RegisteredServiceProvider
import java.util.UUID

class Economy(override val plugin: Plop): Addon, State {

    private lateinit var economyAPI: Economy // Guild API

    override suspend fun load() {
        val rsp: RegisteredServiceProvider<Economy?>? = server.servicesManager.getRegistration(Economy::class.java)
        economyAPI = rsp?.provider ?: throw IllegalStateException("No economy provider found!")
    }

    override suspend fun kill() {
        TODO("Not yet implemented")
    }

    fun hasBalance(player: UUID, amount: Double): Boolean {
        return economyAPI.has(Bukkit.getOfflinePlayer(player), amount)
    }

    fun hasBalance(player: OfflinePlayer, amount: Double): Boolean {
        return economyAPI.has(player, amount)
    }

    fun getBalance(player: UUID): Double {
        return economyAPI.getBalance(Bukkit.getOfflinePlayer(player))
    }

    fun getBalance(player: OfflinePlayer): Double {
        return economyAPI.getBalance(player)
    }

    fun deposit(player: UUID, amount: Double): Boolean {
        return economyAPI.depositPlayer(Bukkit.getOfflinePlayer(player), amount).transactionSuccess()
    }

    fun deposit(player: OfflinePlayer, amount: Double): Boolean {
        return economyAPI.depositPlayer(player, amount).transactionSuccess()
    }

    fun withdraw(player: UUID, amount: Double): Boolean {
        return economyAPI.withdrawPlayer(Bukkit.getOfflinePlayer(player), amount).transactionSuccess()
    }

    fun withdraw(player: OfflinePlayer, amount: Double): Boolean {
        return economyAPI.withdrawPlayer(player, amount).transactionSuccess()
    }
}