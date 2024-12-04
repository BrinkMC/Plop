package com.brinkmc.plop.shared.storage

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.config.SQLData
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

class HikariManager(override val plugin: Plop): Addon, State {

    private lateinit var config: SQLData
    private lateinit var database: HikariDataSource

    override fun load() {
        config = configManager.getDatabaseConfig()
        database.jdbcUrl = "jdbc:mariadb://${config.host}/${config.database}"
        database.username = config.user
        database.password = config.password

        // Get connection and create tables
        try {
            val connection = database.connection
            getSchemaStatements("schema.sql").forEach { statement ->
                connection.prepareStatement(statement).execute()
            }

        }
    }

    override fun kill() {
        TODO("Not yet implemented")
    }

    fun getSchemaStatements(fileName: String): List<String> {
        return plugin.getFile(fileName)?.readText()?.split(";") ?: listOf()
    }

}