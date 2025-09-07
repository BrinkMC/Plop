package com.brinkmc.plop.plot.dto.structure

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


