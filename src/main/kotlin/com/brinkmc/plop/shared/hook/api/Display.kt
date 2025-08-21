package com.brinkmc.plop.shared.hook.api

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import de.oliver.fancyholograms.api.FancyHologramsPlugin
import de.oliver.fancyholograms.api.HologramManager
import de.oliver.fancyholograms.api.data.HologramData
import de.oliver.fancyholograms.api.data.property.Visibility
import de.oliver.fancyholograms.api.hologram.Hologram
import org.bukkit.entity.Player

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
        hologramAPI.addHologram(hologram) // Add hologram to manager
        return hologram
    }

    fun showHologram(player: Player, hologram: Hologram) {
        hologram.forceShowHologram(player)
    }

    fun hideHologram(player: Player, hologram: Hologram) {
        hologram.forceHideHologram(player)
    }
}