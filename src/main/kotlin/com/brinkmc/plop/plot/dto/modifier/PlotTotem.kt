package com.brinkmc.plop.plot.dto.modifier

import com.brinkmc.plop.plot.dto.structure.Totem
import com.brinkmc.plop.plot.constant.TotemType
import org.bukkit.Location

data class PlotTotem(
    val totems: MutableList<Totem>,

    private var _level: Int,
    private var _enableLightning: Boolean
) {
    val level get() = _level
    val enableLightning get() = _enableLightning

    fun setLevel(level: Int) {
        _level = level
    }

    fun setEnableLightning(enable: Boolean) {
        _enableLightning = enable
    }
}