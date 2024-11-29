package com.brinkmc.plop.shared.util

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import org.bukkit.entity.Player

class MessageService(override val plugin: Plop): Addon {

    val plopMessageSource = plugin.plopMessageSource()

    fun notAdmin(): String? {
        return plopMessageSource.findMessage("plop.notadmin")
    }

}