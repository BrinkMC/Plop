package com.brinkmc.plop.plot.plot.modifier

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import org.bukkit.configuration.file.YamlConfiguration
import org.spongepowered.configurate.ConfigurateException
import java.io.File

data class FactoryLimit(
    val level: Int,
    @Transient val factoryLimit: Int
)
