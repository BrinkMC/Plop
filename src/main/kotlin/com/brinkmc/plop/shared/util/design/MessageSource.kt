package com.brinkmc.plop.shared.util.design

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.util.design.enums.MessageKey
import org.spongepowered.configurate.ConfigurateException
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.hocon.HoconConfigurationLoader

class MessageSource(override val plugin: Plop): State, Addon {

    private val hashOfStrings: HashMap<MessageKey, String> = hashMapOf()

    private lateinit var loader: HoconConfigurationLoader

    override suspend fun load() {
        readLocale()
    }

    override suspend fun kill() {
        hashOfStrings.clear()
    }

    private suspend fun readLocale() {
        val localesFile = plugin.getFile("locales.conf")

        // Ensure the plugin stalls if no locale is found
        if (localesFile == null) {
            logger.error("No such file!")
            plugin.kill()
            return
        }

        // Load Configurate library's yaml reader
        loader = HoconConfigurationLoader.builder().file(localesFile).build()

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
                    MessageKey.entries.find { it.name == totalKey }?.let {
                        hashOfStrings[it] = child.string ?: "$totalKey is missing!"
                        logger.info("Registered $totalKey: ${child.string}") // Debug process
                    }
                }
            }
        }
    }

    fun findMessage(find: MessageKey): String? {
        return hashOfStrings[find]
    }
}