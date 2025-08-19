package com.brinkmc.plop.shared.config.configs.plot


import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
data class NexusConfig(
    @Setting("Schematic")
    val schematicName: String = "nexus",

    @Setting("BookName")
    val bookName: String = "<red>Nexus",

    @Setting("Display")
    val display: List<String> = listOf(
        "TEXT:Nexus for <plot_owner>",
        "TEXT:<grey>Right click to open"
    ),

    @Setting("ViewDistance")
    val viewDistance: Int = 100,
)