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

internal fun Location.fullString(): String {
    return "${this.world?.name}:${precision(this.x, 4)}:${precision(this.y, 4)}:${precision(this.z, 4)}:${precision(this.pitch.toDouble(), 4)}:${precision(this.yaw.toDouble(), 4)}"
}

internal fun String.color(): String {
    return ChatColor.translateAlternateColorCodes('&', this)
}

fun precision(x: Double, p: Int): Double {
    val pow = 10.0.pow(p.toDouble())
    return (x * pow).roundToInt() / pow
}

internal fun String.toLocation(): Location? {
    return stringToLocation(this, null)
}

fun stringToLocation(storedLoc: String, forcedWorld: World?): Location? {
    val args = Pattern.compile(":").split(storedLoc)
    if (args.size >= 4 || args.size == 3 && forcedWorld != null) {
        val world = forcedWorld?.name ?: args[0]
        val i = if (args.size == 3) 0 else 1
        val x = args[i].toDouble()
        val y = args[i + 1].toDouble()
        val z = args[i + 2].toDouble()
        val loc = Location(Bukkit.getWorld(world), x, y, z)
        if (args.size >= 6) {
            loc.pitch = args[4].toFloat()
            loc.yaw = args[5].toFloat()
        }
        return loc
    } else if (args.size == 2) {
        val args2: Array<String> = Pattern.compile(",").split(args[1])
        if (args2.size == 3) {
            val world = forcedWorld?.name ?: args[0]
            val x = args2[0].toDouble()
            val y = args2[1].toDouble()
            val z = args2[2].toDouble()
            return Location(Bukkit.getWorld(world), x, y, z)
        }
    }
    return null
}