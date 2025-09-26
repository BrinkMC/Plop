package com.brinkmc.plop.plot.dto.structure

import com.brinkmc.plop.plot.constant.TotemType
import org.bukkit.Location

data class Totem(
    val totemType: TotemType,
    val location: Location
)