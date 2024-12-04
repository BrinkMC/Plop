package com.brinkmc.plop.plot.plot.modifier

import org.bukkit.Location
import java.util.UUID

data class TotemLimit(
    var level: Int,
    var totems: List<UUID>,
    @Transient var totemLimit: Int
)