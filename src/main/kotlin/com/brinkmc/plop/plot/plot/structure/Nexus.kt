package com.brinkmc.plop.plot.plot.structure

import org.bukkit.inventory.ItemStack
import java.util.UUID

data class Nexus(
    private val id: UUID,
    private val location: String,
) {
    fun getId(): UUID {
        return id
    }

    fun getLocation(): String {
        return location
    }
}


