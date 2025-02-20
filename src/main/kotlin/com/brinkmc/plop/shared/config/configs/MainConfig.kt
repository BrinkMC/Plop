package com.brinkmc.plop.shared.config.configs

import com.brinkmc.plop.Plop
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
data class MainConfig(
    @Setting("PluginName")
    val pluginName: String = "Plop"
)