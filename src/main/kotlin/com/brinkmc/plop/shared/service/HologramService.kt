package com.brinkmc.plop.shared.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.factory.controller.display.FactoryDisplay
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.hologram.holograms.NexusDisplay
import com.brinkmc.plop.shared.hologram.holograms.ShopDisplay
import de.oliver.fancyholograms.api.FancyHologramsPlugin
import de.oliver.fancyholograms.api.HologramManager
import de.oliver.fancyholograms.api.data.ItemHologramData
import de.oliver.fancyholograms.api.data.TextHologramData
import de.oliver.fancyholograms.api.data.property.Visibility
import de.oliver.fancyholograms.api.hologram.Hologram

class HologramService(override val plugin: Plop): Addon, State {

    val shopDisplay = ShopDisplay(plugin)
    val nexusDisplay = NexusDisplay(plugin)
    val factoryDisplay = FactoryDisplay(plugin)

    private lateinit var hologramAPI: HologramManager

    override suspend fun load() {
        hologramAPI = FancyHologramsPlugin.get().hologramManager

        listOf(
            shopDisplay,
            nexusDisplay,
            factoryDisplay
        ).forEach { display -> display.load() }
    }

    override suspend fun kill() {
        listOf(
            shopDisplay,
            nexusDisplay,
            factoryDisplay
        ).forEach { display -> display.kill() }
    }

    fun createTextHologram(data: TextHologramData) = hookService.display.createTextHologram(data)

    fun createItemHologram(data: ItemHologramData) = hookService.display.createItemHologram(data)

}