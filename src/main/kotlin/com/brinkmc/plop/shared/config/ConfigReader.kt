package com.brinkmc.plop.shared.config

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.util.async
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

    override suspend fun load() = async {
        readMainConfig()
        readDatabaseConfig()
        readPlotConfig()
        readShopConfig()
        readTotemConfig()
        return@async // Load all config files
    }

    override suspend fun kill() {
        saveMainConfig()
        saveDatabaseConfig()
        savePlotConfig()
        saveShopConfig()
        saveTotemConfig()
        return
    }

    // MAIN CONFIG
    private suspend fun readMainConfig() = async {
        logger.error("Load main config")
        val configFile = plugin.getFile("config.yml")

        configFile?.createNewFile()

        // Load Configurate library's yaml reader
        mainConfigLoader = YamlConfigurationLoader.builder().file(configFile).build()
    }

    private suspend fun saveMainConfig() = async { // Save the config file
        val root = getMainConfig()
        mainConfigLoader.save(root)
    }

    suspend fun getMainConfig(): ConfigurationNode? = async {
        return@async try {
            mainConfigLoader.load() // Allows for direct reading of node values
        } catch (e: ConfigurateException) {
            logger.error(e.message)
            plugin.kill()
            null // Returns null if failed
        }
    }

    // DATABASE CONFIG
    private suspend fun readDatabaseConfig() = async {
        val databaseConfigFile = plugin.getFile("database.yml")

        databaseConfigFile?.createNewFile()

        // Load Configurate library's yaml reader
        databaseConfigLoader = YamlConfigurationLoader.builder().file(databaseConfigFile).build()
    }

    private suspend fun saveDatabaseConfig() = async { // Save the config file
        val root = getDatabaseConfig()
        databaseConfigLoader.save(root)
    }

    suspend fun getDatabaseConfig(): ConfigurationNode? = async {
        return@async try {
            val root = databaseConfigLoader.load()
            root // Returns sql data if valid

        } catch (e: ConfigurateException) {
            logger.error(e.message)
            plugin.kill()
            null // Returns null if failed
        }
    }

    // PLOT CONFIG
    private suspend fun readPlotConfig() = async {
        val plotConfigFile = plugin.getFile("plot.yml")

        plotConfigFile?.createNewFile()
        // Load Configurate library's yaml reader
        plotConfigLoader = YamlConfigurationLoader.builder().file(plotConfigFile).build()
    }

    private suspend fun savePlotConfig() = async { // Save the config file
        val root = getPlotConfig()
        plotConfigLoader.save(root)
    }

    suspend fun getPlotConfig(): ConfigurationNode? = async {
        return@async try {
            val root = plotConfigLoader.load()
            root // Returns sql data if valid

        } catch (e: ConfigurateException) {
            logger.error(e.message)
            plugin.kill()
            null // Returns null if failed
        }
    }

    // SHOP CONFIG
    private suspend fun readShopConfig() = async {
        val shopConfigFile = plugin.getFile("shop.yml")

        shopConfigFile?.createNewFile()
        // Load Configurate library's yaml reader
        shopConfigLoader = YamlConfigurationLoader.builder().file(shopConfigFile).build()
    }

    private suspend fun saveShopConfig() = async { // Save the config file
        val root = getShopConfig()
        shopConfigLoader.save(root)
    }

    suspend fun getShopConfig(): ConfigurationNode? = async {
        return@async try {
            val root = shopConfigLoader.load()
            root // Returns sql data if valid

        } catch (e: ConfigurateException) {
            logger.error(e.message)
            plugin.kill()
            null // Returns null if failed
        }
    }

    private suspend fun readTotemConfig() = async {
        val totemConfigFile = plugin.getFile("totems.yml")

        totemConfigFile?.createNewFile()
        // Load Configurate library's yaml reader
        totemConfigLoader = YamlConfigurationLoader.builder().file(totemConfigFile).build()
    }

    private suspend fun saveTotemConfig() = async { // Save the config file
        val root = getTotemConfig()
        totemConfigLoader.save(root)
    }

    suspend fun getTotemConfig(): ConfigurationNode? = async {
        return@async try {
            val root = totemConfigLoader.load()
            root // Returns sql data if valid

        } catch (e: ConfigurateException) {
            logger.error(e.message)
            plugin.kill()
            null // Returns null if failed
        }
    }
}