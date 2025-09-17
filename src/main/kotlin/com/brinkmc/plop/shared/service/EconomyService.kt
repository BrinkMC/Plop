package com.brinkmc.plop.shared.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import net.milkbowl.vault.economy.Economy
import org.bukkit.plugin.RegisteredServiceProvider
import java.util.UUID

class EconomyService(override val plugin: Plop): Addon, State {

    private lateinit var economy: Economy

    override suspend fun load() {
        val rsp: RegisteredServiceProvider<Economy?>? = server.servicesManager.getRegistration(Economy::class.java)
        economy = rsp?.provider ?: throw IllegalStateException("No economy provider found!")
    }

    override suspend fun kill() {}

    fun getBalance(id: UUID): Double {
        // Identify whether it's player OR guild
        // Return balance
        val player = plugin.server.getPlayer(id)
        val guild = hookService.guilds.getGuild(id)

        if (player != null) {
            return hookService.economy.getBalance(player)
        }

        if (guild != null) {
            return guild.balance
        }

        return 0.0
    }

    fun withdrawBalance(id: UUID, amount: Double): Boolean {
        val player = plugin.server.getPlayer(id)
        val guild = hookService.guilds.getGuild(id)

        if (player != null) {
            val response = economy.withdrawPlayer(player, amount)
            return response.transactionSuccess()
        }

        if (guild != null) {
            if (guild.balance >= amount) {
                guild.balance -= amount
                return true
            }
        }

        return false
    }

    fun depositBalance(id: UUID, amount: Double): Boolean {
        val player = plugin.server.getPlayer(id)
        val guild = hookService.guilds.getGuild(id)

        if (player != null) {
            val response = economy.depositPlayer(player, amount)
            return response.transactionSuccess()
        }

        if (guild != null) {
            guild.balance += amount
            return true
        }

        return false
    }
}