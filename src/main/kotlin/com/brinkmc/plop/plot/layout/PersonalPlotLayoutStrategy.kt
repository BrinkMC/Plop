package com.brinkmc.plop.plot.layout

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.preview.Direction
import com.brinkmc.plop.plot.preview.StringLocation
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.config.configs.PlotConfig
import com.brinkmc.plop.shared.util.collection.LinkedList
import com.brinkmc.plop.shared.util.collection.Node
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.WorldCreator

class PersonalPlotLayoutStrategy(override val plugin: Plop): BaseLayoutStrategy(plugin) {

    override val maxPlotLength: Double = plotConfig.personalPlotMaxSize.toDouble()
    override val maxPreviewLimit: Int
        get() = (plugin.plots.plotMap.size * 3) + 100
    override val worldName: String = plotConfig.personalPlotWorld
    override val worldGen: String = plotConfig.personalPlotWorldGenerator

}