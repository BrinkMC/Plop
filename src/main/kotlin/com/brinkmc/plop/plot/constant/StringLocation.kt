package com.brinkmc.plop.plot.constant

import org.bukkit.Bukkit
import org.bukkit.Location
import java.util.concurrent.atomic.AtomicBoolean

data class StringLocation(
    val world: String,
    val x: Double,
    val y: Double,
    val z: Double,
    private var _free: AtomicBoolean = AtomicBoolean(true)
) {
    var free: Boolean
        get() = _free.get()
        set(value) {
            _free.set(value)
        }

    fun toLocation(): Location {
        return Location(Bukkit.getWorld(world), x, y, z)
    }
}