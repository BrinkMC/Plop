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
    private lateinit var plotConfigLoader: YamlConfigurationLoader
    private lateinit var shopConfigLoader: YamlConfigurationLoader
    private lateinit var totemConfigLoader: YamlConfigurationLoader

    override suspend fun load() {
        readMainConfig()
        readDatabaseConfig()
        readPlotConfig()
        readShopConfig()
        readTotemConfig()
    }

    override suspend fun kill() {
        saveMainConfig()
        saveDatabaseConfig()
        savePlotConfig()
        saveShopConfig()
        saveTotemConfig()
    }

    // MAIN CONFIG
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
    }

    private fun saveMainConfig() { // Save the config file
        val root = getMainConfig()
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

    // DATABASE CONFIG
    private fun readDatabaseConfig() {
        val databaseConfigFile = plugin.getFile("database.yml")

        // Ensure the plugin fails if there is no database
        if (databaseConfigFile == null) {
            logger.error("No such file for database!")
            plugin.kill()
            return
        }

        // Load Configurate library's yaml reader
        databaseConfigLoader = YamlConfigurationLoader.builder().file(databaseConfigFile).build()
    }

    private fun saveDatabaseConfig() { // Save the config file
        val root = getDatabaseConfig()
        databaseConfigLoader.save(root)
    }

    fun getDatabaseConfig(): ConfigurationNode? {
        return try {
            val root = databaseConfigLoader.load()
            root // Returns sql data if valid

        } catch (e: ConfigurateException) {
            logger.error(e.message)
            plugin.kill()
            null // Returns null if failed
        }
    }

    // PLOT CONFIG
    private fun readPlotConfig() {
        val plotConfigFile = plugin.getFile("plot.yml")

        // Ensure the plugin fails if there is no database
        if (plotConfigFile == null) {
            logger.error("No such file for plot!")
            plugin.kill()
            return
        }

        // Load Configurate library's yaml reader
        plotConfigLoader = YamlConfigurationLoader.builder().file(plotConfigFile).build()
    }

    private fun savePlotConfig() { // Save the config file
        val root = getPlotConfig()
        plotConfigLoader.save(root)
    }

    fun getPlotConfig(): ConfigurationNode? {
        return try {
            val root = plotConfigLoader.load()
            root // Returns sql data if valid

        } catch (e: ConfigurateException) {
            logger.error(e.message)
            plugin.kill()
            null // Returns null if failed
        }
    }

    // SHOP CONFIG
    private fun readShopConfig() {
        val shopConfigFile = plugin.getFile("shop.yml")

        // Ensure the plugin fails if there is no database
        if (shopConfigFile == null) {
            logger.error("No such file for shop!")
            plugin.kill()
            return
        }

        // Load Configurate library's yaml reader
        shopConfigLoader = YamlConfigurationLoader.builder().file(shopConfigFile).build()
    }

    private fun saveShopConfig() { // Save the config file
        val root = getShopConfig()
        shopConfigLoader.save(root)
    }

    fun getShopConfig(): ConfigurationNode? {
        return try {
            val root = shopConfigLoader.load()
            root // Returns sql data if valid

        } catch (e: ConfigurateException) {
            logger.error(e.message)
            plugin.kill()
            null // Returns null if failed
        }
    }

    private fun readTotemConfig() {
        val totemConfigFile = plugin.getFile("totems.yml")

        // Ensure the plugin fails if there is no database
        if (totemConfigFile == null) {
            logger.error("No such file for totems!")
            plugin.kill()
            return
        }

        // Load Configurate library's yaml reader
        totemConfigLoader = YamlConfigurationLoader.builder().file(totemConfigFile).build()
    }

    private fun saveTotemConfig() { // Save the config file
        val root = getTotemConfig()
        totemConfigLoader.save(root)
    }

    fun getTotemConfig(): ConfigurationNode? {
        return try {
            val root = totemConfigLoader.load()
            root // Returns sql data if valid

        } catch (e: ConfigurateException) {
            logger.error(e.message)
            plugin.kill()
            null // Returns null if failed
        }
    }
}