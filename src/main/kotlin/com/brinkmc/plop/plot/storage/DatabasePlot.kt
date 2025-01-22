package com.brinkmc.plop.plot.storage

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.plot.plot.data.PlotClaim
import com.brinkmc.plop.plot.plot.data.PlotVisitState
import com.brinkmc.plop.plot.plot.modifier.PlotFactory
import com.brinkmc.plop.plot.plot.modifier.PlotSize
import com.brinkmc.plop.plot.plot.modifier.PlotShop
import com.brinkmc.plop.plot.plot.modifier.PlotTotem
import com.brinkmc.plop.plot.plot.modifier.PlotVisitLimit
import com.brinkmc.plop.plot.plot.structure.TOTEM_TYPE
import com.brinkmc.plop.plot.plot.structure.Totem
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.util.Funcs.fullString
import com.brinkmc.plop.shared.util.Funcs.toLocation
import com.brinkmc.plop.shared.util.async
import org.bukkit.Location
import java.sql.ResultSet
import java.sql.Timestamp
import java.util.UUID

/*
Implements all methods used to communicate with the database + cache
 */

class DatabasePlot(override val plugin: Plop): Addon, State {

    override suspend fun load() {}

    override suspend fun kill() {}

    suspend fun load(uuid: UUID): Plot? = async {
        val query = """
        SELECT * FROM plots_plots plots WHERE plots.plot_id = ?
        INNER JOIN plots_claims claim ON claim.plot_id = plots.plot_id
        INNER JOIN plots_sizes size ON size.plot_id = plots.plot_id
        INNER JOIN plots_factory_limits factory_lim ON factory_lim.plot_id = plots.plot_id
        INNER JOIN plots_shop_limits shop_lim ON shop_lim.plot_id = plots.plot_id
        INNER JOIN plots_visitor_limits visitor_lim ON visitor_lim.plot_id = plots.plot_id
        INNER JOIN plots_visits visit ON visits.plot_id = plots.plot_id
    """.trimIndent() // Draws complete dataset for 1 plot
        val resultSet = DB.query(query, uuid.toString())
        return@async resultSet?.let { mapToPlot(it) }
    }

    private suspend fun mapToPlot(resultSet: ResultSet): Plot = async {
        resultSet.next() // Get the first result (should be the only result)

        val plotId = UUID.fromString(resultSet.getString("plot_id"))
        val plotType: PlotType = PlotType.valueOf(resultSet.getString("type"))
        val ownerId = UUID.fromString(resultSet.getString("owner_id"))

        val plotClaim = PlotClaim(
            centre = resultSet.getString("centre").toLocation() ?: throw IllegalArgumentException("Invalid centre location"), // If it fails to load, throw exception
            home = resultSet.getString("home").toLocation() ?: throw IllegalArgumentException("Invalid home location"),
            world = resultSet.getString("world") ?: throw IllegalArgumentException("Invalid world"),
            visit = resultSet.getString("visit").toLocation() ?: throw IllegalArgumentException("Invalid visit location")
        )

        // Set visitor
        val plotVisitLimit = PlotVisitLimit(
            level = resultSet.getInt("visitor_lim.level"), // Get the stored level
            currentAmount = resultSet.getInt("visitor_lim.amount"),
            plotType = plotType
        )

        val plotSize = PlotSize(
            level = resultSet.getInt("size.level"),
            plotType = plotType
            // amount = plotConfig.getPlotSizeLevels(plotType)[resultSet.getInt("size.level")]

        )

        // Get list of factories
        val factoryList = mutableListOf<Location>()
        val subFactoriesQuerySet = DB.query("SELECT * FROM plots_factory_locations WHERE plot_id=?", plotId)
        if (subFactoriesQuerySet != null) do { // Only execute if there are factories
            factoryList.add(subFactoriesQuerySet.getString("factory_location").toLocation()!!) // Must be a location
        } while (subFactoriesQuerySet.next())

        val plotFactory = PlotFactory(
            level = resultSet.getInt("factory_lim.level"),
            factories = factoryList,
            plotType = plotType
        )

        // Get list of shops
        val shopList = mutableListOf<UUID>()
        val subShopQuerySet = DB.query("SELECT * FROM plots_shop_locations WHERE plot_id=?", plotId)
        if (subShopQuerySet != null) do { // Only execute if there are shops
            shopList.add(UUID.fromString(subShopQuerySet.getString("shop_id"))) // Can't be null
        } while (subShopQuerySet.next())
        val plotShop = PlotShop(
            level = resultSet.getInt("shop_lim.level"),
            shops = shopList,
            plotType = plotType
        )

        val subTotemResultSet = DB.query("SELECT * FROM plots_totems WHERE plot_id = ?", plotId)
        val subVisitsResultSet = DB.query("SELECT * FROM plots_visit_records WHERE plot_id = ?", plotId)

        val totems = mutableListOf<Totem>()
        val visitRecords = mutableListOf<Timestamp>()

        // Add all totems
        while (subTotemResultSet?.next() == true) {
            val totemId = subTotemResultSet.getInt("totem_id")
            val totemType = TOTEM_TYPE.valueOf(subTotemResultSet.getString("totem_type"))
            val totemLocation = subTotemResultSet.getString("totem_location").toLocation()
                ?: throw IllegalArgumentException("Invalid totem location")
            totems.add(Totem(totemId, totemType, totemLocation))
        }

        // Add all visit timestamps
        while (subVisitsResultSet?.next() == true) {
            val visitTimestamp = subVisitsResultSet.getTimestamp("record")
            visitRecords.add(visitTimestamp)
        }

        val plotTotem = PlotTotem(
            level = resultSet.getInt("totem_level"),
            totems = totems,
            plotType = plotType
        )

        val plotVisitState = PlotVisitState(
            resultSet.getBoolean("allow_visitors"),
            visits = visitRecords,

        )

        val plotVisit = PlotVisitState(resultSet.getBoolean("allow_visitors"), visitRecords)

        return@async Plot(
            plotId,
            type = plotType,
            ownerId = ownerId,
            claim = plotClaim,
            visitState = plotVisitState,
            visitLimit = plotVisitLimit,
            size = plotSize,
            factory = plotFactory,
            shop = plotShop,
            totem = plotTotem
        )
    }

    suspend fun create(plot: Plot) = async {
        DB.update("INSERT INTO plots_plots (plot_id, type, owner_id) VALUES(?, ?, ?)",
            plot.plotId,
            plot.type.toString(),
            plot.ownerId
        )
        DB.update("INSERT INTO plots_claims (max_length, centre, home, visit, plot_id) VALUES (?, ?, ?, ?, ?)",
            plot.claim.centre.fullString(false),
            plot.claim.home.fullString(),
            plot.claim.visit.fullString(),
            plot.plotId
        )
        // Skip totems, none placed
        DB.update("INSERT INTO plots_sizes (level, plot_id) VALUES(?, ?)",
            plot.size.level,
            plot.plotId
        )
        DB.update("INSERT INTO plots_factory_limits (level, plot_id) VALUES(?, ?)",
            plot.factory.level,
            plot.plotId
        )
        // Skip factories placed, none placed
        DB.update("INSERT INTO plots_shop_limits (level, plot_id) VALUES(?, ?)",
            plot.shop.level,
            plot.plotId
        )
        // Skip shops placed, none placed
        DB.update("INSERT INTO plots_visitor_limits (level, plot_id) VALUES(?, ?)",
            plot.visitLimit.level,
            plot.plotId
        )
        DB.update("INSERT INTO plots_visits (allow_visitors, plot_id) VALUES(?, ?)",
            plot.visitState.visitable,
            plot.plotId
        )
        // No visitor records
    }

    suspend fun save(plot: Plot) = async {
        DB.update("INSERT INTO plots_plots (plot_id, type, owner_id) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE type=?, owner_id=?",
            plot.plotId,
            plot.type.toString(),
            plot.ownerId,
            plot.type.toString(),
            plot.ownerId
        )
        DB.update("INSERT INTO plots_claims (max_length, centre, home, visit, plot_id) VALUES(?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE max_length=?, centre=?, home=?, visit=?",
            plot.claim.centre.fullString(false),
            plot.claim.home.fullString(),
            plot.claim.visit.fullString(),
            plot.plotId,
            plot.claim.centre.fullString(false),
            plot.claim.home.fullString(),
            plot.claim.visit.fullString(),
        )
        for (totem in plot.totem.totems) {
            DB.update("INSERT INTO plots_totems (totem_location, totem_type, plot_id) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE totem_type=?",
                totem.location.fullString(false),
                totem.totemType.toString(),
                plot.plotId,
                totem.totemType.toString()
            )
        }
        DB.update("INSERT INTO plots_sizes (level, plot_id) VALUES(?, ?) ON DUPLICATE KEY UPDATE level=?",
            plot.size.level,
            plot.plotId,
            plot.size.level
        )
        DB.update("INSERT INTO plots_factory_limits (level, plot_id) VALUES(?, ?) ON DUPLICATE KEY UPDATE level=?",
            plot.factory.level,
            plot.plotId,
            plot.factory.level
        )
        for (shop in plot.shop.shops) {
            DB.update("INSERT INTO plots_shop_locations (shop_id, plot_id) VALUES(?, ?) ON DUPLICATE KEY UPDATE plot_id?",
                shop,
                plot.plotId,
                plot.plotId // Not sure about this one
            )
        }
        DB.update("INSERT INTO plots_shop_limits (level, plot_id) VALUES(?, ?) ON DUPLICATE KEY UPDATE level=?",
            plot.shop.level,
            plot.plotId,
            plot.shop.level
        )
        for (factory in plot.factory.factories) {
            DB.update("INSERT INTO plots_factory_locations (factory_location, plot_id) VALUES(?, ?) ON DUPLICATE KEY UPDATE plot_id=?",
                factory.fullString(false),
                plot.plotId,
                plot.plotId // Not sure about this one
            )
        }
        DB.update("INSERT INTO plots_visitor_limits (level, plot_id) VALUES(?, ?) ON DUPLICATE KEY UPDATE level=?",
            plot.visitLimit.level,
            plot.plotId,
            plot.visitLimit.level
        )
        DB.update("INSERT INTO plots_visits (allow_visitors, plot_id) VALUES(?, ?) ON DUPLICATE KEY UPDATE allow_visitors=?",
            plot.visitState.visitable,
            plot.plotId,
            plot.visitState.visitable
        )
    }

    // Should hopefully never have to be called
    suspend fun delete(plot: Plot) = async {
        val deleteUpdate =  """
            DELETE FROM plots_plots, plots_claims, plots_totems, plots_sizes, plots_factory_limits, plots_factory_locations, plots_shop_limits, plots_visitor_limits, plots_visits, plots_visit_records) WHERE plot_id=?
        """.trimIndent() // Delete all references to the plot
        DB.update(deleteUpdate, plot.plotId)
    }
}