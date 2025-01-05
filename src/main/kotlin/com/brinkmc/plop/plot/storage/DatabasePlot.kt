package com.brinkmc.plop.plot.storage

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.PLOT_TYPE
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.plot.plot.data.Claim
import com.brinkmc.plop.plot.plot.data.PlotVisit
import com.brinkmc.plop.plot.plot.modifier.FactoryLimit
import com.brinkmc.plop.plot.plot.modifier.PlotSize
import com.brinkmc.plop.plot.plot.modifier.ShopLimit
import com.brinkmc.plop.plot.plot.modifier.VisitorLimit
import com.brinkmc.plop.plot.plot.structure.TOTEM_TYPE
import com.brinkmc.plop.plot.plot.structure.Totem
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.storage.HikariManager
import com.brinkmc.plop.shared.util.toLocation
import java.sql.ResultSet
import java.sql.Timestamp
import java.util.Date
import java.util.UUID

/*
Implements all methods used to communicate with the database + cache
 */

internal class DatabasePlot(override val plugin: Plop): Addon, State {

    override fun load() {}

    override fun kill() {}

    fun load(uuid: UUID): Plot? {
        val query = """
        SELECT * FROM plots_plots plots WHERE plots.plot_id = ?
        INNER JOIN plots_claims claim ON claim.plot_id = plots.plot_id
        INNER JOIN plots_sizes size ON size.plot_id = plots.plot_id
        INNER JOIN plots_factory_limits factory_lim ON factory_lim.plot_id = plots.plot_id
        INNER JOIN plots_shop_limits shop_lim ON shop_lim.plot_id = plots.plot_id
        INNER JOIN plots_visitor_limits visitor_lim ON visitor_lim.plot_id = plots.plot_id
        INNER JOIN plots_visits visit ON visits.plot_id = plots.plot_id
    """.trimIndent() // Draws compelete dataset for 1 plot
        val resultSet = DB.query(query, uuid.toString())
        return resultSet?.let { mapToPlot(it) }
    }

    private fun mapToPlot(resultSet: ResultSet): Plot {
        resultSet.next() // Get the first result (should be the only result)

        val plotId = UUID.fromString(resultSet.getString("plot_id"))
        val type: PLOT_TYPE = PLOT_TYPE.valueOf(resultSet.getString("type"))
        val ownerId = UUID.fromString(resultSet.getString("owner_id"))

        val claim = Claim(
            maxLength = resultSet.getInt("max_length"),
            centre = resultSet.getString("centre").toLocation() ?: throw IllegalArgumentException("Invalid centre location"), // If it fails to load, throw exception
            home = resultSet.getString("home").toLocation() ?: throw IllegalArgumentException("Invalid home location"),
            visit = resultSet.getString("visit").toLocation() ?: throw IllegalArgumentException("Invalid visit location")
        )

        val visitorLimit = VisitorLimit(
            level = resultSet.getInt("visitor_lim.level"), // Get the stored level
            amount = 0, //TODO FIX THIS LOL
            visitorLimit = plots.plotVisitorHandler.levels[resultSet.getInt("visitor_lim.level")] // Read from config
        )

        val plotSize = PlotSize(
            level = resultSet.getInt("size.level"),
            size = plots.plotSizeHandler.levels[resultSet.getInt("size.level")]
        )

        val factoryLimit = FactoryLimit(
            level = resultSet.getInt("factory_lim.level"),
            factories = listOf(), //TODO FIX THIS
            factoryLimit = plots.plotFactoryHandler.levels[resultSet.getInt("factory_lim.level")]
        )

        val shopLimit = ShopLimit(
            level = resultSet.getInt("shop_lim.level"),
            shops = listOf(), //TODO FIX THIS
            shopLimit = plots.plotShopHandler.levels[resultSet.getInt("shop_lim.level")]
        )

        val subQueryTotem = """
            SELECT * FROM plots_totems WHERE plot_id = ?
        """.trimIndent()
        val subTotemResultSet = DB.query(subQueryTotem, plotId)

        val subQueryVisits = """
            SELECT * FROM plots_visit_records WHERE plot_id = ?
        """.trimIndent()
        val subVisitsResultSet = DB.query(subQueryVisits, plotId)

        if (subTotemResultSet == null || subVisitsResultSet == null) {
            return Plot(plotId,
                type,
                ownerId,
                claim,
                visitorLimit,
                plotSize,
                factoryLimit,
                shopLimit,
                mutableListOf(), // Set totems to empty
                PlotVisit() // Default state of a plot visit settings
            )
        }

        val totems = mutableListOf<Totem>()
        val visitRecords = mutableListOf<Timestamp>()

        // Add all totems
        do {
            val totemId = subTotemResultSet.getInt("totem_id")
            val totemType = TOTEM_TYPE.valueOf(subTotemResultSet.getString("totem_type"))
            val totemLocation = subTotemResultSet.getString("totem_location").toLocation() ?: throw IllegalArgumentException("Invalid totem location")
            totems.add(Totem(totemId, totemType, totemLocation))

        } while (subTotemResultSet.next())

        // Add all visit timestamps
        do {
            val visitTimestamp = subVisitsResultSet.getTimestamp("record")
            visitRecords.add(visitTimestamp)
        } while (subVisitsResultSet.next())

        val plotVisit = PlotVisit(resultSet.getBoolean("allow_visitors"), visitRecords)

        return Plot(plotId,
            type,
            ownerId,
            claim,
            visitorLimit,
            plotSize,
            factoryLimit,
            shopLimit,
            totems, // Actual list of totems
            plotVisit // Real plot visits loaded
        )
    }

    fun create(plot: Plot) {}
}