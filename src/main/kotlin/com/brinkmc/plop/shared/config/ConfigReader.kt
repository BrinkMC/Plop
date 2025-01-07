package com.brinkmc.plop.shared.config

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.sun.org.apache.xalan.internal.utils.ConfigurationError
import org.spongepowered.configurate.ConfigurateException
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.loader.ConfigurationLoader
import org.spongepowered.configurate.yaml.YamlConfigurationLoader

class ConfigReader(override val plugin: Plop): Addon, State {


    private lateinit var mainConfigLoader: YamlConfigurationLoader
    private lateinit var databaseConfigLoader: YamlConfigurationLoader

    override fun load() {
        readMainConfig()
        readDatabaseConfig()
    }

    override fun kill() {
        saveMainConfig()
        saveDatabaseConfig()

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

    private fun saveMainConfig() { // Save the config file
        val root = try {
            mainConfigLoader.load()
        }
        catch (e: ConfigurateException) {
            logger.error(e.message)
            plugin.kill()
            return
        }

        mainConfigLoader.save(root)
    }

    fun getMainConfig(): ConfigurationNode? {
        return try {
            mainConfigLoader.load() // Allows for direct reading of node values
        } catch (e: ConfigurateException) {
            logger.error(e.message)
            plugin.kill()
            null // Returns null if failed
        }
    }

    private fun readDatabaseConfig() {
        val databaseConfigFile = plugin.getFile("database.yml")

        // Ensure the plugin fails if there is no database
        if (databaseConfigFile == null) {
            logger.error("No such file for database!")
            plugin.kill()
            return
        }

        // Load
        databaseConfigLoader = YamlConfigurationLoader.builder().file(databaseConfigFile).build()

        // Try load the configuration, exception will kill plugin
        val root = try {
            databaseConfigLoader.load()
        }
        catch (e: ConfigurateException) {
            logger.error(e.message)
            plugin.kill()
            return
        }


    }

    private fun saveDatabaseConfig() { // Save the config file
        val root = try {
            databaseConfigLoader.load()
        }
        catch (e: ConfigurateException) {
            logger.error(e.message)
            plugin.kill()
            return
        }

        databaseConfigLoader.save(root)
    }

    fun getDatabaseConfig(): SQLData? {
        return try {
            val root = databaseConfigLoader.load()

            val sqlData = SQLData()
            sqlData.user = root.node("username").string ?: "ERROR"
            sqlData.password = root.node("password").string ?: "ERROR"
            sqlData.database = root.node("database").string ?: "ERROR"
            sqlData.host = root.node("host").string ?: "ERROR"

            sqlData // Returns sql data if valid

        } catch (e: ConfigurateException) {
            logger.error(e.message)
            plugin.kill()
            null // Returns null if failed
        }
    }
}