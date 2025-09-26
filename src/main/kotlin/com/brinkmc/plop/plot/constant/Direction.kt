package com.brinkmc.plop.plot.constant

import org.bukkit.Bukkit
import org.bukkit.Location
import java.util.concurrent.atomic.AtomicBoolean

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

