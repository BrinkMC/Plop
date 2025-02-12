package com.brinkmc.plop.plot.storage.database

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.plot.plot.modifier.PlotClaim
import com.brinkmc.plop.plot.plot.modifier.PlotFactory
import com.brinkmc.plop.plot.plot.modifier.PlotShop
import com.brinkmc.plop.plot.plot.modifier.PlotSize
import com.brinkmc.plop.plot.plot.modifier.PlotTotem
import com.brinkmc.plop.plot.plot.modifier.PlotVisit
import com.brinkmc.plop.plot.plot.structure.TOTEM_TYPE
import com.brinkmc.plop.plot.plot.structure.Totem
import com.brinkmc.plop.plot.storage.PlotKey
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.util.Funcs.fullString
import com.brinkmc.plop.shared.util.Funcs.toLocation
import org.bukkit.Location
import java.sql.ResultSet
import java.sql.Timestamp
import java.util.UUID

class DatabasePlot(override val plugin: Plop): Addon, State {

    override suspend fun load() {}

    override suspend fun kill() {}

    private suspend fun loadPlotId(plotId: UUID): Plot? {
        return asyncScope {
            val query = """
        SELECT * FROM plots_plots AS plots 
        INNER JOIN plots_claims AS claim ON claim.plot_id = plots.plot_id
        INNER JOIN plots_sizes AS size ON size.plot_id = plots.plot_id
        INNER JOIN plots_factory_limits AS factory_lim ON factory_lim.plot_id = plots.plot_id
        INNER JOIN plots_shop_limits AS shop_lim ON shop_lim.plot_id = plots.plot_id
        INNER JOIN plots_visitor_limits AS visitor_lim ON visitor_lim.plot_id = plots.plot_id
        INNER JOIN plots_visits AS visit ON visits.plot_id = plots.plot_id 
        WHERE plots.plot_id = ?
    """.trimIndent() // Draws complete dataset for 1 plot
            val resultSet = DB.query(query, plotId.toString())
            return@asyncScope resultSet?.let { mapToPlot(it) }
        }
    }

    private suspend fun loadPlotOwner(ownerId: UUID): Plot? {
        return asyncScope {
            val query = """
        SELECT * FROM plots_plots AS plots
        INNER JOIN plots_claims AS claim ON claim.plot_id = plots.plot_id
        INNER JOIN plots_sizes AS size ON size.plot_id = plots.plot_id
        INNER JOIN plots_factory_limits AS factory_lim ON factory_lim.plot_id = plots.plot_id
        INNER JOIN plots_shop_limits AS shop_lim ON shop_lim.plot_id = plots.plot_id
        INNER JOIN plots_visitor_limits AS visitor_lim ON visitor_lim.plot_id = plots.plot_id
        INNER JOIN plots_visits AS visit ON visit.plot_id = plots.plot_id
        WHERE plots.owner_id = ?
    """.trimIndent() // Draws complete dataset for 1 plot using owner
            val resultSet = DB.query(query, ownerId.toString())
            return@asyncScope resultSet?.let { mapToPlot(it) }
        }
    }

    suspend fun load(plotKey: PlotKey): Plot  {
        return if (plotKey.plotId != null) {
            loadPlotId(plotKey.plotId) ?: throw IllegalArgumentException("Plot not found")

        } else if (plotKey.ownerId != null) {
            loadPlotOwner(plotKey.ownerId) ?: throw IllegalArgumentException("Plot not found")

        } else {
            throw IllegalArgumentException("Invalid plot key")
        }
    }

    private suspend fun mapToPlot(resultSet: ResultSet): Plot {
        return asyncScope {
            resultSet.next() // Get the first result (should be the only result)

            val plotId = UUID.fromString(resultSet.getString("plot_id"))
            val plotType: PlotType = PlotType.valueOf(resultSet.getString("type"))
            val ownerId = UUID.fromString(resultSet.getString("owner_id"))

            val plotClaim = PlotClaim(
                centre = resultSet.getString("centre").toLocation()
                    ?: throw IllegalArgumentException("Invalid centre location"), // If it fails to load, throw exception
                home = resultSet.getString("home").toLocation()
                    ?: throw IllegalArgumentException("Invalid home location"),
                world = resultSet.getString("world") ?: throw IllegalArgumentException("Invalid world"),
                visit = resultSet.getString("visit").toLocation()
                    ?: throw IllegalArgumentException("Invalid visit location")
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

            // Set visitor
            val plotVisit = PlotVisit(
                visitable = resultSet.getBoolean("visitor_lim.allow_visitors"),
                level = resultSet.getInt("visitor_lim.level"), // Get the stored level
                currentVisits = resultSet.getInt("visitor_lim.current_amount"),
                historicalVisits = visitRecords,
                plotType = plotType
            )

            return@asyncScope Plot(
                plotId,
                type = plotType,
                ownerId = ownerId,
                claim = plotClaim,
                visit = plotVisit,
                size = plotSize,
                factory = plotFactory,
                shop = plotShop,
                totem = plotTotem
            )
        }
    }

    suspend fun create(plot: Plot) {
        DB.update("INSERT INTO plots_plots (plot_id, type, owner_id) VALUES(?, ?, ?)",
            plot.plotId,
            plot.type.toString(),
            plot.ownerId
        )
        DB.update("INSERT INTO plots_claims (centre, home, visit, plot_id) VALUES (?, ?, ?, ?, ?)",
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
        DB.update("INSERT INTO plots_visitor_limits (level, plot_id, current_amount, allow_visitors) VALUES(?, ?, ?, ?)",
            plot.visit.level,
            plot.plotId,
            plot.visit.currentVisits,
            plot.visit.visitable
        )
        // No visitor records
    }

    suspend fun save(plot: Plot) {
        // Update or insert into plots_plots
        DB.update(
            "INSERT INTO plots_plots (plot_id, type, owner_id) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE type = VALUES(type), owner_id = VALUES(owner_id)",
            plot.plotId,
            plot.type.toString(),
            plot.ownerId
        )

        // Update or insert into plots_claims
        DB.update(
            "INSERT INTO plots_claims (centre, home, visit, plot_id) VALUES(?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE centre = VALUES(centre), home = VALUES(home), visit = VALUES(visit)",
            plot.claim.centre.fullString(false),
            plot.claim.home.fullString(),
            plot.claim.visit.fullString(),
            plot.plotId
        )

        // Update or insert into plots_sizes
        DB.update(
            "INSERT INTO plots_sizes (level, plot_id) VALUES(?, ?) ON DUPLICATE KEY UPDATE level = VALUES(level)",
            plot.size.level,
            plot.plotId
        )

        // Update or insert into plots_factory_limits
        DB.update(
            "INSERT INTO plots_factory_limits (level, plot_id) VALUES(?, ?) ON DUPLICATE KEY UPDATE level = VALUES(level)",
            plot.factory.level,
            plot.plotId
        )

        // Update plots_factory_locations (handling list of factories)
        for (factory in plot.factory.factories) {
            DB.update(
                "INSERT INTO plots_factory_locations (factory_location, plot_id) VALUES(?, ?) ON DUPLICATE KEY UPDATE plot_id = VALUES(plot_id)",
                factory.fullString(false),
                plot.plotId
            )
        }

        // Update or insert into plots_shop_limits
        DB.update(
            "INSERT INTO plots_shop_limits (level, plot_id) VALUES(?, ?) ON DUPLICATE KEY UPDATE level = VALUES(level)",
            plot.shop.level,
            plot.plotId
        )

        // Update plots_shop_locations (handling list of shops)
        for (shop in plot.shop.shops) {
            DB.update(
                "INSERT INTO plots_shop_locations (shop_id, plot_id) VALUES(?, ?) ON DUPLICATE KEY UPDATE plot_id = VALUES(plot_id)",
                shop,
                plot.plotId
            )
        }

        // Update plots_visitor_limits
        DB.update(
            "INSERT INTO plots_visitor_limits (allow_visitors, level, current_amount, plot_id) VALUES(?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE allow_visitors = VALUES(allow_visitors), level = VALUES(level), current_amount = VALUES(current_amount)",
            plot.visit.visitable,
            plot.visit.level,
            plot.visit.currentVisits,
            plot.plotId
        )

        // Update plots_totems (handling list of totems)
        for (totem in plot.totem.totems) {
            DB.update(
                "INSERT INTO plots_totems (totem_location, totem_type, plot_id) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE totem_type = VALUES(totem_type)",
                totem.location.fullString(false),
                totem.totemType.toString(),
                plot.plotId
            )
        }
    }


    // Should hopefully never have to be called
    suspend fun delete(plot: Plot) {
        // Individual table deletions to honor relationships and dependencies
        DB.update("DELETE FROM plots_visit_records WHERE plot_id = ?", plot.plotId)
        DB.update("DELETE FROM plots_visitor_limits WHERE plot_id = ?", plot.plotId)
        DB.update("DELETE FROM plots_shop_locations WHERE plot_id = ?", plot.plotId)
        DB.update("DELETE FROM plots_shop_limits WHERE plot_id = ?", plot.plotId)
        DB.update("DELETE FROM plots_factory_locations WHERE plot_id = ?", plot.plotId)
        DB.update("DELETE FROM plots_factory_limits WHERE plot_id = ?", plot.plotId)
        DB.update("DELETE FROM plots_totems WHERE plot_id = ?", plot.plotId)
        DB.update("DELETE FROM plots_sizes WHERE plot_id = ?", plot.plotId)
        DB.update("DELETE FROM plots_claims WHERE plot_id = ?", plot.plotId)
        DB.update("DELETE FROM plots_plots WHERE plot_id = ?", plot.plotId)
    }

    suspend fun addVisit(plot: Plot) {
        val timestamp = Timestamp(System.currentTimeMillis())
        DB.update(
            "INSERT INTO plots_visit_records (plot_id, record) VALUES(?, ?)",
            plot.plotId,
            timestamp
        )
    }
}