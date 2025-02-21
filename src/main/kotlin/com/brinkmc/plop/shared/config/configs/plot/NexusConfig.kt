package com.brinkmc.plop.shared.config.configs.plot

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
data class NexusConfig(
    @Setting("Schematic")
    val schematicName: String = "nexus",

    @Setting("BookName")
    val bookName: String = "<red>Nexus",
)