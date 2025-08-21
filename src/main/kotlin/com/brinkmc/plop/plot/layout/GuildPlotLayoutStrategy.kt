package com.brinkmc.plop.plot.layout

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.PlotType

class GuildPlotLayoutStrategy(override val plugin: Plop, override val plotType: PlotType): BaseLayoutStrategy(plugin, plotType) {

    override val maxPlotLength: Double = plotConfig.getPlotMaxSize(plotType).toDouble()
    override val maxPreviewLimit: Int
        get() = (plugin.hooks.worldGuard.getPlotRegions(plotType).size * 3) + 100
    override val worldName: String = plotConfig.getPlotWorld(plotType)
    override val worldGen: String = plotConfig.getPlotWorldGenerator(plotType)

}