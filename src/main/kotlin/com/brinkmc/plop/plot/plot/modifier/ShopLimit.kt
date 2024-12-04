package com.brinkmc.plop.plot.plot.modifier

import org.bukkit.Location

data class ShopLimit(
    var level: Int,
    var shops: List<Location>,
    @Transient var shopLimit: Int
)