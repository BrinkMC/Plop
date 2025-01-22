package com.brinkmc.plop.shared.util

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.spongepowered.configurate.ConfigurateException
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.yaml.YamlConfigurationLoader

class PlopMessageSource(override val plugin: Plop): State, Addon {

    private val hashOfStrings: HashMap<String, String> = hashMapOf()

    private lateinit var loader: YamlConfigurationLoader

    override suspend fun load() {
        readLocale()
    }

    override suspend fun kill() {
        hashOfStrings.clear()
        loader
    }

    private suspend fun readLocale() {
        val localesFile = plugin.getFile("locales.yml")

        // Ensure the plugin stalls if no locale is found
        if (localesFile == null) {
            logger.error("No such file!")
            plugin.kill()
            return
        }

        // Load Configurate library's yaml reader
        loader = YamlConfigurationLoader.builder().file(localesFile).build()

        // Try load the configuration, exception will kill plugin
        val root = try {
             loader.load()
        }
        catch (e: ConfigurateException) {
            logger.error(e.message)
            plugin.kill()
            return
        }

        parseNode(root, "")
    }

    // Recursive function - Revision 1 didn't realise this fact
    private fun parseNode(node: ConfigurationNode, parentKey: String) {
        node.childrenMap().forEach { (key, child) ->
            val childKey = key.toString()
            // For recursion, find key of the config value + for hashmap usage
            val totalKey = if (parentKey.isEmpty()) childKey else "$parentKey.$childKey"

            if (!child.empty()) {
                // Recursive look at children nodes
                if (child.isMap) {
                    parseNode(child, totalKey)
                }
                // Not a child node with children and is instead a value
                else {
                    hashOfStrings[totalKey] = child.string ?: "$totalKey is missing!"
                    logger.debug("Registered $totalKey: ${child.string}") // Debug process
                }
            }
        }
    }

    fun findMessage(find: String): String? {
        return hashOfStrings[find]
    }


}