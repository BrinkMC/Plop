package com.brinkmc.plop.shared.config

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.config.configs.MainConfig
import com.brinkmc.plop.shared.config.configs.PlotConfig
import com.brinkmc.plop.shared.config.configs.SQLConfig
import com.brinkmc.plop.shared.config.configs.ShopConfig
import com.brinkmc.plop.shared.config.configs.TotemConfig
import com.brinkmc.plop.shared.config.serialisers.Level
import com.brinkmc.plop.shared.config.serialisers.LevelSerialiser
import org.spongepowered.configurate.ConfigurateException
import org.spongepowered.configurate.hocon.HoconConfigurationLoader
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.extensions.set
import org.spongepowered.configurate.kotlin.objectMapperFactory

class ConfigHandler(override val plugin: Plop): Addon, State {

    // Public services
    val mainConfig: MainConfig
        get() = _mainConfig

    val plotConfig: PlotConfig
        get() = _plotConfig

    val shopConfig: ShopConfig
        get() = _shopConfig

    val sqlConfig: SQLConfig
        get() = _sqlConfig

    val totemConfig: TotemConfig
        get() = _totemConfig

    private lateinit var mainConfigLoader: HoconConfigurationLoader
    private lateinit var databaseConfigLoader: HoconConfigurationLoader
    private lateinit var plotConfigLoader: HoconConfigurationLoader
    private lateinit var shopConfigLoader: HoconConfigurationLoader
    private lateinit var totemConfigLoader: HoconConfigurationLoader

    private lateinit var _mainConfig: MainConfig
    private lateinit var _sqlConfig: SQLConfig
    private lateinit var _plotConfig: PlotConfig
    private lateinit var _shopConfig: ShopConfig
    private lateinit var _totemConfig: TotemConfig

    override suspend fun load() {
        readDatabaseConfig()
        readMainConfig()
        readPlotConfig()
        readShopConfig()
        readTotemConfig()
        saveMainConfig()
        saveDatabaseConfig()
        savePlotConfig()
        saveShopConfig()
        saveTotemConfig()
    }

    override suspend fun kill() { plugin.syncScope {
        saveMainConfig()
        saveDatabaseConfig()
        savePlotConfig()
        saveShopConfig()
        saveTotemConfig()
    } }

    // MAIN CONFIG
    private fun readMainConfig() {
        logger.info("Load main config") // Load main config file
        val configFile = plugin.getFile("config.conf")

        configFile?.createNewFile()

        mainConfigLoader = HoconConfigurationLoader.builder()
            .file(configFile)
            .defaultOptions { options ->
                options.serializers { builder ->
                    builder.registerAnnotatedObjects(objectMapperFactory())
                }
            }
            .build()
        _mainConfig = mainConfigLoader.load().get(MainConfig::class) ?: run {
            logger.error("Failed to load main config")
            return
        }
    }

    private fun saveMainConfig() { // Save the config file
        try {
            val root = mainConfigLoader.load()
            root.set(MainConfig::class, mainConfig)
            mainConfigLoader.save(root)
        } catch (e: ConfigurateException) {
            logger.error(e.message)
        }
    }

    // DATABASE CONFIG
    private fun readDatabaseConfig() {
        logger.info("Load database config") // Load database config file
        val configFile = plugin.getFile("sql.conf")

        configFile?.createNewFile()

        databaseConfigLoader = HoconConfigurationLoader.builder()
            .file(configFile)
            .defaultOptions { options ->
                options.serializers { builder ->
                    builder.registerAnnotatedObjects(objectMapperFactory())
                }
            }
            .build()

        _sqlConfig = databaseConfigLoader.load().get(SQLConfig::class) ?: run {
            logger.error("Failed to load database config")
            _sqlConfig = SQLConfig()
            return
        }
    }

    private fun saveDatabaseConfig() { // Save the config file
        try {
            val root = databaseConfigLoader.load()
            root.set(SQLConfig::class, sqlConfig)
            databaseConfigLoader.save(root)
        } catch (e: ConfigurateException) {
            logger.error(e.message)
        }
    }

    // PLOT CONFIG
    private fun readPlotConfig() {
        logger.info("Load plot config") // Load plot config file
        val configFile = plugin.getFile("plot.conf")

        configFile?.createNewFile()

        plotConfigLoader = HoconConfigurationLoader.builder()
            .file(configFile)
            .defaultOptions { options ->
                options.serializers { builder ->
                    builder.registerAnnotatedObjects(objectMapperFactory())
                    builder.register(Level::class.java, LevelSerialiser())
                }
            }
            .build()
        _plotConfig = plotConfigLoader.load().get(PlotConfig::class) ?: run {
            logger.error("Failed to load plot config")
            return
        }
    }

    private fun savePlotConfig() { // Save the config file
        try {
            val root = plotConfigLoader.load()
            root.set(PlotConfig::class, plotConfig)
            plotConfigLoader.save(root)
        } catch (e: ConfigurateException) {
            logger.error(e.message)
        }
    }

    // SHOP CONFIG
    private fun readShopConfig() {
        logger.info("Load shop config") // Load shop config file
        val configFile = plugin.getFile("shop.conf")

        configFile?.createNewFile()

        shopConfigLoader = HoconConfigurationLoader.builder()
            .file(configFile)
            .defaultOptions { options ->
                options.serializers { builder ->
                    builder.registerAnnotatedObjects(objectMapperFactory())
                }
            }
            .build()
        _shopConfig = shopConfigLoader.load().get(ShopConfig::class) ?: run {
            logger.error("Failed to load shop config")
            return
        }
    }

    private fun saveShopConfig() { // Save the config file
        try {
            val root = shopConfigLoader.load()
            root.set(ShopConfig::class, shopConfig)
            shopConfigLoader.save(root)
        } catch (e: ConfigurateException) {
            logger.error(e.message)
        }
    }

    // TOTEM CONFIG
    private fun readTotemConfig() {
        logger.info("Load totem config") // Load totem config file
        val configFile = plugin.getFile("totem.conf")

        configFile?.createNewFile()

        totemConfigLoader = HoconConfigurationLoader.builder()
            .file(configFile)
            .defaultOptions { options ->
                options.serializers { builder ->
                    builder.registerAnnotatedObjects(objectMapperFactory())
                }
            }
            .build()
        _totemConfig = totemConfigLoader.load().get(TotemConfig::class) ?: run {
            logger.error("Failed to load totem config")
            return
        }
    }

    private fun saveTotemConfig() { // Save the config file
        try {
            val root = totemConfigLoader.load()
            root.set(TotemConfig::class, totemConfig)
            totemConfigLoader.save(root)
        } catch (e: ConfigurateException) {
            logger.error(e.message)
        }
    }
}