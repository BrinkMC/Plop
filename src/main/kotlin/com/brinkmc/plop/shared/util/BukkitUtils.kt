package com.brinkmc.plop.shared.util

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID

object BukkitUtils {
    fun UUID.player(): Player? {
        return Bukkit.getPlayer(this)
    }

}