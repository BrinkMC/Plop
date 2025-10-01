package com.brinkmc.plop.shared.dto

import de.oliver.fancyholograms.api.hologram.Hologram
import org.joml.Vector3f

data class HologramWrapper(
    val id: String, // ID of hologram, linked to shop, nexus, factory e.t.c
    val hologram: Hologram,
    val offset: Double,
    val scale: Vector3f
)
