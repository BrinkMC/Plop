package com.brinkmc.plop.plot.dto.logs

import com.brinkmc.plop.plot.constant.Actionable
import com.brinkmc.plop.plot.constant.PlotAction
import com.brinkmc.plop.plot.constant.PlotActionInstance
import com.brinkmc.plop.plot.constant.PlotLog
import org.bukkit.Location
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class PlotLogs (
    private var _logs: MutableList<PlotLog> = mutableListOf()

) {
    val logs: List<PlotLog>
        get() = _logs

    @OptIn(ExperimentalTime::class)
    fun recordLog(log: Actionable, actor: UUID) {
        val timestampNow: Double = Clock.System.now().epochSeconds.toDouble()
        _logs.add(PlotLog(log, actor, timestampNow))
    }
}