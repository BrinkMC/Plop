package com.brinkmc.plop.shared.util

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import org.bukkit.Bukkit

class PlopMessageSource(override val plugin: Plop): State, Addon {

    val hashOfStrings: HashMap<String, String> = hashMapOf()

    fun readLocale() {
        val localesFile = plugin.getFile("locales.yml")

        if(localesFile == null) {
            logger.error("No such file!")
            plugin.kill()
        }

    }

    fun findMessage(find: String): String? {
        return hashOfStrings[find]
    }
}