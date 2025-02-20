package com.brinkmc.plop.shared.storage

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.zaxxer.hikari.HikariDataSource
import java.sql.ResultSet

class HikariManager(override val plugin: Plop): Addon, State {

    private lateinit var database: HikariDataSource

    override suspend fun load() {

        // Copy schema.sql to resources
        plugin.saveResource("schema.sql", false)

        database = HikariDataSource()

        database.jdbcUrl = "jdbc:mysql://${sqlConfig.host}/${sqlConfig.database}"
        database.username = sqlConfig.user
        database.password = sqlConfig.password

        // Get connection and create tables
        try {
            val connection = database.connection
            getSchemaStatements("schema.sql").forEach { statement ->
                if (statement.isNotEmpty()) {
                    connection.prepareStatement(statement).execute()
                }
            }
        }
        catch (e: Exception) {
            logger.error("${e.message}")
            return
        }
    }

    override suspend fun kill() { asyncScope {
        database.close() // Kill database connection
    }}

    // Query the database, returns results
    suspend fun query(query: String, vararg params: Any): ResultSet? {
        return asyncScope {
            return@asyncScope try {
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