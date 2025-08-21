package com.brinkmc.plop.plot.plot.modifier

import java.sql.Timestamp

data class PlotVisit(
    private var visitable: Boolean = true, // Is plot visitable
    private var level: Int,
    private var currentVisits: Int,
    private val historicalVisits: MutableList<Timestamp> = mutableListOf()
) {
    fun isVisitable(): Boolean {
        return visitable
    }

    fun setVisitable(visitable: Boolean) {
        this.visitable = visitable
    }

    fun getLevel(): Int {
        return level
    }

    fun upgradeLevel() {
        level++
    }

    fun getCurrentVisits(): Int {
        return currentVisits
    }

    private fun incrementVisits() {
        currentVisits++
    }

    fun decrementVisits() {
        if (currentVisits > 0) {
            currentVisits--
        }
    }

    fun getHistoricalVisits(): List<Timestamp> {
        return historicalVisits
    }

    fun addVisit(timestamp: Timestamp) {
        incrementVisits()
        historicalVisits.add(timestamp)
    }
}