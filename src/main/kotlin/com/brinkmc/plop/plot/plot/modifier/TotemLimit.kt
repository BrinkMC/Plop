package com.brinkmc.plop.plot.plot.modifier

data class TotemLimit(
    val level: Int,
    @Transient val totemLimit: Int
)