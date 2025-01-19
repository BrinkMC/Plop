package com.brinkmc.plop.plot.plot.modifier

import org.bukkit.Location
import java.util.UUID

data class ShopLimit(
    var level: Int,
    var shops: List<UUID>
)