package com.brinkmc.plop.shared.hooks

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import de.oliver.fancyholograms.api.FancyHologramsPlugin
import de.oliver.fancyholograms.api.HologramManager
import de.oliver.fancyholograms.api.data.HologramData
import de.oliver.fancyholograms.api.data.property.Visibility
import de.oliver.fancyholograms.api.hologram.Hologram
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.api.GuildsAPI
import me.glaremasters.guilds.api.events.GuildCreateEvent
import me.glaremasters.guilds.guild.Guild
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import java.util.UUID

class Display(override val plugin: Plop): Addon, State {

    private lateinit var hologramAPI: HologramManager // Guild API

    override suspend fun load() {
        hologramAPI = FancyHologramsPlugin.get().hologramManager
    }

    override suspend fun kill() {}

    fun createHologram(hologramData: HologramData): Hologram {
        val hologram = hologramAPI.create(hologramData)
        hologram.data.isPersistent = false // DO NOT SAVE
        hologram.data.visibility = Visibility.MANUAL
        return hologram
    }

    fun showHologram(player: Player, hologram: Hologram) {
        hologram.forceShowHologram(player)
    }

    fun hideHologram(player: Player, hologram: Hologram) {
        hologram.forceHideHologram(player)
    }
}