package com.brinkmc.plop.shared.hook.api

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import de.oliver.fancyholograms.api.FancyHologramsPlugin
import de.oliver.fancyholograms.api.HologramManager
import de.oliver.fancyholograms.api.data.HologramData
import de.oliver.fancyholograms.api.data.TextHologramData
import de.oliver.fancyholograms.api.data.property.Visibility
import de.oliver.fancyholograms.api.hologram.Hologram
import java.util.UUID

class FancyHologram(override val plugin: Plop): Addon, State {

    private lateinit var hologramAPI: HologramManager // Hologram API

    override suspend fun load() {
        hologramAPI = FancyHologramsPlugin.get().hologramManager
    }

    override suspend fun kill() {}

    fun createTextHologram(data: TextHologramData): Hologram {
        val hologram = hologramAPI.create(data)
        hologram.data.isPersistent = false
        hologram.data.visibility = Visibility.MANUAL
        hologramAPI.addHologram(hologram)
        return hologram
    }

    fun createItemHologram(data: HologramData): Hologram {
        val hologram = hologramAPI.create(data)
        hologram.data.isPersistent = false
        hologram.data.visibility = Visibility.MANUAL
        hologramAPI.addHologram(hologram)
        return hologram
    }


}