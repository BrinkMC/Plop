package com.brinkmc.plop.plot.dto.modifier

import com.brinkmc.plop.plot.dto.structure.Nexus
import org.bukkit.Location

data class PlotNexus(
    private val nexus: MutableList<Nexus>
) {
    fun addNexus(nexus: Nexus) {
        this.nexus.add(nexus)
    }

    fun removeNexus(location: String) {
        this.nexus.removeIf { it.location == location }
    }
}