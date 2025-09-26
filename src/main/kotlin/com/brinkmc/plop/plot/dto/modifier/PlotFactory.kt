package com.brinkmc.plop.plot.dto.modifier

data class PlotFactory(
    private var _level: Int
) {
    val level: Int get() = _level

    fun setLevel(level: Int) {
        _level = level
    }
}