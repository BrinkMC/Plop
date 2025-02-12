package com.brinkmc.plop.shared.config.configs

import com.brinkmc.plop.Plop
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
data class TotemConfig(
    @Setting("Totem")
    val totem: String?
)