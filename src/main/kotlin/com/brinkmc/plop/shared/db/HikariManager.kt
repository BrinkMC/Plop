package com.brinkmc.plop.shared.db

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection
import java.sql.ResultSet
import java.util.concurrent.LinkedBlockingQueue

class HikariManager(override val plugin: Plop): Addon, State {

    private lateinit var database: HikariDataSource

    override suspend fun load() {

        // Copy schema.sql to resources
        plugin.saveResource("schema.sql", false)

        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:mysql://${configService.sqlConfig.host}/${configService.sqlConfig.database}"
            username = configService.sqlConfig.user
            password = configService.sqlConfig.password
            addDataSourceProperty("cachePrepStmts", "true")
            addDataSourceProperty("prepStmtCacheSize", "250")
            addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
            maximumPoolSize = 10
            minimumIdle = 2
            connectionTimeout = 30000L
            maxLifetime = 600000L
        }

        database = HikariDataSource(config)

        // Get connection and create tables
        try {
            database.connection.use { connection ->
                getSchemaStatements("schema.sql").forEach { statement ->
                    if (statement.isNotEmpty()) { connection.prepareStatement(statement).execute() }
                }
            }
        }
        catch (e: Exception) {
            logger.error("${e.message}")
            return
        }
    }

    override suspend fun kill() { plugin.asyncScope {
        if (::database.isInitialized) database.close()
    }}

    // Query the database, returns results
    suspend fun <T> query(query: String, vararg params: Any, mapper: (ResultSet) -> T): T? = plugin.asyncScope {
        return@asyncScope try {
            database.connection.use { connection ->
                connection.prepareStatement(query).use { statement ->
                    params.forEachIndexed { index, param -> statement.setObject(index + 1, param) }
                    statement.executeQuery().use {
                        mapper(it)
                    }
                }
            }
        } catch (exception: Exception) { // Code has failed catastrophically
            logger.error("Failed to query MySQL :( -> ${exception.message}")
            null
        }
    }

    suspend fun update(query: String, vararg params: Any): Int? = plugin.asyncScope {
        return@asyncScope try {
            database.connection.use { connection ->
                connection.prepareStatement(query).use { statement ->
                    params.forEachIndexed { index, param -> statement.setObject(index + 1, param) }
                    statement.executeUpdate()
                }
            }
        } catch (exception: Exception) { // Code has failed catastrophically
            logger.error("Failed to update MySQL :( -> ${exception.message}")
            null
        }
    }


    fun getSchemaStatements(fileName: String): List<String> {
        return plugin.getResource(fileName)?.bufferedReader()?.readText()
            ?.split(";")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            ?: emptyList()
    }
}