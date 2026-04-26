package com.brinkmc.plop.plot.dto.modifier

import java.sql.Timestamp
import java.util.UUID

data class PlotVisit(


    private var _visitable: Boolean = true, // Is plot visitable
    private var _level: Int,
    private var _votes: Int = 0,

    val currentVisits: MutableList<UUID>,
    val historicalVisits: MutableList<Timestamp> = mutableListOf(),
) {
    val level get() = _level
    val visitable get() = _visitable
    val votes get() = _votes

    fun setLevel(level: Int) {
        _level = level
    }

    fun setVisitable(visitable: Boolean) {
        _visitable = visitable
    }

    fun addVote() {
        _votes += 1
    }

    fun resetVotes() {
        _votes = 0
    }

    fun addVisitor(visitorId: UUID) {
        if (currentVisits.contains(visitorId)) return
        currentVisits.add(UUID.randomUUID())
    }

    fun removeVisitor(visitorId: UUID) {
        currentVisits.remove(UUID.randomUUID())
    }
}