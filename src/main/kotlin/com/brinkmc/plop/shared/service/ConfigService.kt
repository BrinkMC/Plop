package com.brinkmc.plop.shared.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.config.configs.FactoryConfig
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
import kotlin.collections.get
import kotlin.collections.set

class ConfigService(override val plugin: Plop) : Addon, State {

    private data class ConfigHolder<T : Any>(
        val fileName: String,
        val clazz: Class<T>,
        val loaderFactory: (java.io.File) -> HoconConfigurationLoader
    )

    private val configs = listOf(
        ConfigHolder("config.conf", MainConfig::class.java) { file ->
            HoconConfigurationLoader.builder()
                .file(file)
                .defaultOptions { opts ->
                    opts.serializers { s -> s.registerAnnotatedObjects(objectMapperFactory()) }
                }
                .build()
        },
        ConfigHolder("sql.conf", SQLConfig::class.java) { file ->
            HoconConfigurationLoader.builder()
                .file(file)
                .defaultOptions { opts ->
                    opts.serializers { s -> s.registerAnnotatedObjects(objectMapperFactory()) }
                }
                .build()
        },
        ConfigHolder("plot.conf", PlotConfig::class.java) { file ->
            HoconConfigurationLoader.builder()
                .file(file)
                .defaultOptions { opts ->
                    opts.serializers { s ->
                        s.registerAnnotatedObjects(objectMapperFactory())
                        s.register(Level::class.java, LevelSerialiser())
                    }
                }
                .build()
        },
        ConfigHolder("shop.conf", ShopConfig::class.java) { file ->
            HoconConfigurationLoader.builder()
                .file(file)
                .defaultOptions { opts ->
                    opts.serializers { s -> s.registerAnnotatedObjects(objectMapperFactory()) }
                }
                .build()
        },
        ConfigHolder("totem.conf", TotemConfig::class.java) { file ->
            HoconConfigurationLoader.builder()
                .file(file)
                .defaultOptions { opts ->
                    opts.serializers { s -> s.registerAnnotatedObjects(objectMapperFactory()) }
                }
                .build()
        },
        ConfigHolder("factory.conf", FactoryConfig::class.java) { file ->
            HoconConfigurationLoader.builder()
                .file(file)
                .defaultOptions { opts ->
                    opts.serializers { s -> s.registerAnnotatedObjects(objectMapperFactory()) }
                }
                .build()
        }
    )

    private val configLoaders = mutableMapOf<String, HoconConfigurationLoader>()
    private val configData = mutableMapOf<String, Any>()

    val mainConfig get() = getConfig<MainConfig>()
    val plotConfig get() = getConfig<PlotConfig>()
    val shopConfig get() = getConfig<ShopConfig>()
    val sqlConfig get() = getConfig<SQLConfig>()
    val totemConfig get() = getConfig<TotemConfig>()
    val factoryConfig get() = getConfig<FactoryConfig>()

    override suspend fun load() {
        configs.forEach { readConfig(it) }
        configs.forEach { saveConfig(it) }
    }

    override suspend fun kill() = plugin.syncScope {
        configs.forEach { saveConfig(it) }
    }

    private fun <T : Any> readConfig(holder: ConfigHolder<T>) {
        logger.info("Load ${holder.fileName}")
        val configFile = plugin.getFile(holder.fileName)
        configFile.createNewFile()
        val loader = holder.loaderFactory(configFile)
        configLoaders[holder.fileName] = loader
        val config = loader.load().get(holder.clazz) ?: run {
            logger.error("Failed to load ${holder.fileName}")
            null
        }
        if (config != null) configData[holder.fileName] = config
    }

    private fun <T : Any> saveConfig(holder: ConfigHolder<T>) {
        try {
            val loader = configLoaders[holder.fileName] ?: return
            val config = configData[holder.fileName] as? T ?: return
            val root = loader.load()
            root.set(holder.clazz, config)
            loader.save(root)
        } catch (e: ConfigurateException) {
            logger.error(e.message)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private inline fun <reified T : Any> getConfig(): T =
        configData.values.first { it is T } as T
}