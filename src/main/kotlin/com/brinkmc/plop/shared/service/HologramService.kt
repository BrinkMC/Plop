package com.brinkmc.plop.shared.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.hologram.holograms.NexusDisplay
import com.brinkmc.plop.shared.hologram.holograms.ShopDisplay

class HologramService(override val plugin: Plop): Addon, State {
    val shopDisplay = ShopDisplay(plugin)
    val nexusDisplay = NexusDisplay(plugin)
    val factoryDisplay = FactoryDisplay(plugin)

    override suspend fun load() {
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
}