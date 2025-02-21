package com.brinkmc.plop.shared.storage

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection
import java.sql.ResultSet
import java.util.concurrent.LinkedBlockingQueue

class HikariManager(override val plugin: Plop): Addon, State {

    private lateinit var database: HikariDataSource
    private val connections = LinkedBlockingQueue<Connection>()

    fun getConnection(): Connection {
        return connections.take()
    }

    fun releaseConnection(connection: Connection) {
        connections.put(connection)
    }

    fun closeAllConnections() {
        connections.forEach { connection ->
            connection.close()
        }
    }

    override suspend fun load() {

        // Copy schema.sql to resources
        plugin.saveResource("schema.sql", false)

        database = HikariDataSource()

        database.jdbcUrl = "jdbc:mysql://${sqlConfig.host}/${sqlConfig.database}"
        database.username = sqlConfig.user
        database.password = sqlConfig.password

        try {
            for (i in 1..7) {
                connections.add(database.connection)
            }
        } catch (e: Exception) {
            logger.error("${e.message}")
            return
        }

        // Get connection and create tables
        try {
            val connection = getConnection()
            getSchemaStatements("schema.sql").forEach { statement ->
                if (statement.isNotEmpty()) {
                    connection.prepareStatement(statement).execute()
                }
            }
            releaseConnection(connection)
        }
        catch (e: Exception) {
            logger.error("${e.message}")
            return
        }
    }

    override suspend fun kill() { asyncScope {
        closeAllConnections() // Close all connections
        database.close() // Kill database
    }}

    // Query the database, returns results
    suspend fun query(query: String, vararg params: Any): ResultSet? {
        return asyncScope {
            return@asyncScope try {
                val connection = getConnection()
                val preparedStatement = connection.prepareStatement(query)
                params.forEachIndexed { index, param -> // "Prevent" SQL injection
                    preparedStatement.setObject(index + 1, param)
                }
                val resultSet = preparedStatement.executeQuery() // Actually execute and returns
                releaseConnection(connection)
                resultSet
            } catch (exception: Exception) { // Code has failed catastrophically
                logger.error("Failed to query MySQL :( -> ${exception.message}")
                null
            }
        }
    }

    suspend fun update(query: String, vararg params: Any): Int? {
        return try {
            val connection = getConnection() // Establish connection
            val preparedStatement = connection.prepareStatement(query)
            params.forEachIndexed { index, param -> // "Prevent" SQL injection
                preparedStatement.setObject(index + 1, param)
            }
            val result = preparedStatement.executeUpdate() // Updates the code and returns number of lines affected
            releaseConnection(connection)
            result
        } catch (exception: Exception) { // Code has failed catastrophically
            logger.error("Failed to update MySQL :( -> ${exception.message}")
            null
        }
    }


    suspend fun getSchemaStatements(fileName: String): List<String> {
        return plugin.getFile(fileName)?.readText()?.split(";") ?: listOf()
    }

}