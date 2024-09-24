package com.brinkmc.plop.plot.plot.modifier

data class VisitorLimit(
    val level: Int,
    @Transient val visitorLimit: Int
)