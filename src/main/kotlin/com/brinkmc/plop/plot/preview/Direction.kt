package com.brinkmc.plop.plot.preview

enum class Direction {
    NORTH, EAST, SOUTH, WEST;

    fun next(): Direction {
        return when (this) {
            NORTH -> EAST
            EAST -> SOUTH
            SOUTH -> WEST
            WEST -> NORTH
        }
    }
}

data class StringLocation(
    val x: Double,
    val y: Double,
    val z: Double
)