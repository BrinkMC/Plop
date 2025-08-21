package com.brinkmc.plop.shared.hologram

import com.brinkmc.plop.shared.hologram.holograms.NexusDisplay
import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.hologram.holograms.ShopDisplay


class HologramHandler(val plugin: Plop): State {
    val shopDisplay = ShopDisplay(plugin)
    val nexusDisplay = NexusDisplay(plugin)

    override suspend fun load() {
        listOf(
            shopDisplay,
            nexusDisplay
        ).forEach { display -> display.load() }
    }

    override suspend fun kill() {
        listOf(
            shopDisplay,
            nexusDisplay
        ).forEach { display -> display.kill() }
    }
}