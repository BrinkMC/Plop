package com.brinkmc.plop.shared.config.configs

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.config.BaseConfig
import org.spongepowered.configurate.ConfigurationNode
import kotlin.reflect.KProperty

data class SQLConfig(
    override val plugin: Plop, // Must be able to access the plugin
): BaseConfig(plugin) {

    override suspend fun loadConfig(): ConfigurationNode? {
        return configManager.getDatabaseConfig()
    } // Database config file

    // Get values for the SQL Config, this lays out the schema of the file
    var user: String by delegate("user")
    var password: String by delegate("password")
    var database: String by delegate("database")
    var host: String by delegate("host")

}