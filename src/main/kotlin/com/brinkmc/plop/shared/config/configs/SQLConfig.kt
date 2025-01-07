package com.brinkmc.plop.shared.config.configs

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon

data class SQLConfig(
    override val plugin: Plop // Must be able to access the plugin
): Addon {
    val databaseConfig = configManager.getDatabaseConfig() // Database config file

    // Get values for the SQL Config, this lays out the schema of the file
    var user: String
        get() = databaseConfig?.node("username")?.string ?: ""
        set(value) {
            databaseConfig?.node("name")?.set(value)
        }

    var password: String
        get() = databaseConfig?.node("password")?.string ?: ""
        set(value) {
            databaseConfig?.node("password")?.set(value)
        }

    var database: String
        get() = databaseConfig?.node("database")?.string ?: ""
        set(value) {
            databaseConfig?.node("database")?.set(value)
        }

    var host: String
        get() = databaseConfig?.node("host")?.string ?: ""
        set(value) {
            databaseConfig?.node("host")?.set(value)
        }
}