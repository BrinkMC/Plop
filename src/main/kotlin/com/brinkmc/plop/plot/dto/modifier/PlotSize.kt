package com.brinkmc.plop.plot.dto.modifier


data class PlotSize(
    private var _level: Int
) {
    val level get() = _level

    fun setLevel(level: Int) {
        _level = level
    }
}