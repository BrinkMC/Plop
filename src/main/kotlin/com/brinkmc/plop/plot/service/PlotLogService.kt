package com.brinkmc.plop.plot.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.constant.Actionable
import com.brinkmc.plop.plot.constant.PlotAction
import com.brinkmc.plop.plot.constant.PlotActionInstance
import com.brinkmc.plop.plot.constant.PlotLog
import com.brinkmc.plop.plot.constant.PlotType
import com.brinkmc.plop.plot.dto.logs.PlotLogs
import com.brinkmc.plop.plot.dto.modifier.PlotShop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.config.serialisers.Level
import com.brinkmc.plop.shared.constant.MessageKey
import com.brinkmc.plop.shared.constant.ServiceResult
import com.brinkmc.plop.shared.constant.SoundKey
import java.util.UUID

class PlotLogService(override val plugin: Plop): Addon, State {

    override suspend fun load() {

    }

    override suspend fun kill() {

    }

    // Getters

    private suspend fun getPlotLogs(plotId: UUID): PlotLogs? = plotService.getPlotLogs(plotId)

    suspend fun getLogs(plotId: UUID): List<PlotLog> {
        val plotLogs = getPlotLogs(plotId) ?: return emptyList()
        return plotLogs.logs
    }

    // Setters

    suspend fun addPlotLog(plotId: UUID, action: Actionable, actor: UUID) {
        val plotLogs = getPlotLogs(plotId) ?: return
        plotLogs.recordLog(action, actor)
    }
}