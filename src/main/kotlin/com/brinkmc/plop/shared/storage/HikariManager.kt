package com.brinkmc.plop.shared.storage

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.config.configs.SQLConfig
import com.brinkmc.plop.shared.util.DispatcherContainer.sync
import com.brinkmc.plop.shared.util.async
import com.brinkmc.plop.shared.util.sync
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.ResultSet

class HikariManager(override val plugin: Plop): Addon, State {

    private lateinit var database: HikariDataSource

    override suspend fun load() = async {

        database.jdbcUrl = "jdbc:mariadb://${SQLConfig.host}/${SQLConfig.database}"
        database.username = SQLConfig.user
        database.password = SQLConfig.password

        // Get connection and create tables
        try {
            val connection = database.connection
            getSchemaStatements("schema.sql").forEach { statement ->
                connection.prepareStatement(statement).execute()
            }
        }
        catch (e: Exception) {
            logger.error("${e.message}")
            return@async
        }
    }

    override suspend fun kill() = async {
        database.close() // Kill database connection
    }

    // Query the database, returns results
    suspend fun query(query: String, vararg params: Any): ResultSet? = async {
        return@async try {
            val connection = database.connection
            val preparedStatement = connection.prepareStatement(query)
            params.forEachIndexed { index, param -> // "Prevent" SQL injection
                preparedStatement.setObject(index + 1, param)
            }
            preparedStatement.executeQuery() // Actually execute and returns
        } catch (exception: Exception) { // Code has failed catastrophically
            logger.error("Failed to query MySQL :( -> ${exception.message}")
            null
        }
    }

    suspend fun update(query: String, vararg params: Any): Int? {
        return try {
            val connection = database.connection // Establish connection
            val preparedStatement = connection.prepareStatement(query)
            params.forEachIndexed { index, param -> // "Prevent" SQL injection
                preparedStatement.setObject(index + 1, param)
            }
            preparedStatement.executeUpdate() // Updates the code and returns number of lines affected

        } catch (exception: Exception) { // Code has failed catastrophically
            logger.error("Failed to update MySQL :( -> ${exception.message}")
            null
        }
    }


    suspend fun getSchemaStatements(fileName: String): List<String> {
        return plugin.getFile(fileName)?.readText()?.split(";") ?: listOf()
    }

}