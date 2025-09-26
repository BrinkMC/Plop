package com.brinkmc.plop.plot.dto.modifier

import com.brinkmc.plop.plot.dto.structure.Fueler

data class PlotFueler(
    private var _level: Int, // Depending on level you can't place higher level fuelers
    private var _fueler: Fueler?
) {
    val level: Int
        get() = _level

    val fueler: Fueler?
        get() = _fueler

    fun setLevel(level: Int) {
        _level = level
    }

    fun setFueler(fueler: Fueler) {
        _fueler = fueler
    }

}