package com.brinkmc.plop.shared.util


import java.util.regex.Pattern
import kotlin.math.pow
import kotlin.math.roundToInt
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.World

/*
Credit to DarbyJack in his plugin Guilds for these handy extensions
 */

object Funcs {
    internal fun Location.fullString(includeYawPitch: Boolean = true): String { // Default to true
        return if (includeYawPitch) {
            "${this.world?.name}:${precision(this.x, 4)}:${precision(this.y, 4)}:${precision(this.z, 4)}:${precision(this.pitch.toDouble(), 4)}:${precision(this.yaw.toDouble(), 4)}"
        } else {
            "${this.world?.name}:${precision(this.x, 4)}:${precision(this.y, 4)}:${precision(this.z, 4)}"
        }
    }

    fun precision(x: Double, p: Int): Double { // Rounding algorithm
        val pow = 10.0.pow(p.toDouble())
        return (x * pow).roundToInt() / pow
    }

    internal fun String.toLocation(): Location? {
        return stringToLocation(this)
    }

    private fun stringToLocation(locationString: String): Location? {
        val parts = locationString.split(":")
        if (parts.size !in 4..6) return null // Make sure string is correct size to be real location

        val world = Bukkit.getWorld(parts[0]) ?: return null
        val x = parts[1].toDoubleOrNull() ?: return null
        val y = parts[2].toDoubleOrNull() ?: return null
        val z = parts[3].toDoubleOrNull() ?: return null
        val yaw = if (parts.size > 4) parts[4].toFloatOrNull() ?: 0.0f else 0.0f // Provide default value if null
        val pitch = if (parts.size > 5) parts[5].toFloatOrNull() ?: 0.0f else 0.0f // Provide default value if null

        return Location(world, x, y, z, yaw, pitch)
    }
}


