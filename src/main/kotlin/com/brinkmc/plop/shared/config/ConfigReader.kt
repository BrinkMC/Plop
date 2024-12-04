package com.brinkmc.plop.shared.config

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import org.spongepowered.configurate.ConfigurateException
import org.spongepowered.configurate.yaml.YamlConfigurationLoader

class ConfigReader(override val plugin: Plop): Addon, State {


    lateinit var mainConfigLoader: YamlConfigurationLoader

    override fun load() {
        readMainConfig()
    }

    override fun kill() {
        // Nothing to kill
    }

    private fun readMainConfig() {
        val configFile = plugin.getFile("config.yml")

        // Ensure the plugin stalls if no locale is found
        if (configFile == null) {
            logger.error("No such file!")
            plugin.kill()
            return
        }

        // Load Configurate library's yaml reader
        mainConfigLoader = YamlConfigurationLoader.builder().file(configFile).build()

        // Try load the configuration, exception will kill plugin
        val root = try {
            mainConfigLoader.load()
        }
        catch (e: ConfigurateException) {
            logger.error(e.message)
            plugin.kill()
            return
        }
    }
}