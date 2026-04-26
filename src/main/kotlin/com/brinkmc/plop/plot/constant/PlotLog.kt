package com.brinkmc.plop.plot.constant

import java.util.UUID

data class PlotLog(
    val action: Actionable,
    val actor: UUID,
    val time: Double
)
