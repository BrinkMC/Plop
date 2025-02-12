package com.brinkmc.plop.plot.handler

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import org.bukkit.configuration.file.YamlConfiguration
import org.spongepowered.configurate.ConfigurateException
import java.io.File

class PlotShopHandler(override val plugin: Plop): Addon, State {

    val levels = mutableListOf<Int>()

    override suspend fun load() {
    }

    override suspend fun kill() {
        levels.clear()
    }
}