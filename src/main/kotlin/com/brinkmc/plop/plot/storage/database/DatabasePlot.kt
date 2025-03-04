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
import com.brinkmc.plop.plot.plot.structure.Totem
import com.brinkmc.plop.plot.plot.structure.TotemType
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

    suspend fun load(plotId: UUID): Plot? {
        val id = plotId
        val plot = loadPlotCore(id) ?: return null
        plot.claim = loadClaim(id) ?: plot.claim
        plot.size = loadSize(id) ?: plot.size
        plot.factory = loadFactory(id, plot.type)
        plot.shop = loadShop(id, plot.type)
        plot.totem = loadTotem(id, plot.type)
        plot.visit = loadVisits(id, plot.type)
        plot.nexus = loadNexus(id)
        return plot
    }

    private suspend fun loadPlotCore(plotId: UUID): Plot? {
        val rs = DB.query("SELECT * FROM plots WHERE plot_id=?", plotId.toString()) ?: return null
        return if (rs.next()) {
            Plot(
                plotId = plotId,
                type = PlotType.valueOf(rs.getString("type")),
                nexus = mutableListOf(),
                claim = PlotClaim(Location(null, 0.0, 0.0, 0.0), Location(null,0.0,0.0,0.0), Location(null,0.0,0.0,0.0)),
                visit = PlotVisit(true,0,0, mutableListOf(),PlotType.PERSONAL),
                size = PlotSize(0,PlotType.PERSONAL),
                factory = PlotFactory(0, mutableListOf(),PlotType.PERSONAL),
                shop = PlotShop(0, mutableListOf(),PlotType.PERSONAL),
                totem = PlotTotem(0, mutableListOf(),true,PlotType.PERSONAL)
            )
        } else null
    }

    private suspend fun loadClaim(plotId: UUID): PlotClaim? {
        val rs = DB.query("SELECT * FROM plot_claims WHERE plot_id=?", plotId.toString()) ?: return null
        return if (rs.next()) {
            PlotClaim(
                centre = rs.getString("centre").toLocation() ?: return null,
                home = rs.getString("home").toLocation() ?: return null,
                visit = rs.getString("visit").toLocation() ?: return null
            )
        } else null
    }

    private suspend fun loadSize(plotId: UUID): PlotSize? {
        val rs = DB.query("SELECT * FROM plot_sizes WHERE plot_id=?", plotId.toString()) ?: return null
        return if (rs.next()) {
            PlotSize(rs.getInt("size_level"), PlotType.PERSONAL)
        } else null
    }

    private suspend fun loadFactory(plotId: UUID, plotType: PlotType): PlotFactory {
        val rs = DB.query("SELECT * FROM plot_factories WHERE plot_id=?", plotId.toString())
        var level = 0
        if (rs?.next() == true) {
            level = rs.getInt("factory_level")
        }
        val locations = mutableListOf<Location>()
        val rs2 = DB.query("SELECT * FROM plot_factory_locations WHERE plot_id=?", plotId.toString())
        while (rs2?.next() == true) {
            rs2.getString("factory_location").toLocation()?.let { locations.add(it) }
        }
        return PlotFactory(level, locations, plotType)
    }

    private suspend fun loadShop(plotId: UUID, plotType: PlotType): PlotShop {
        val rs = DB.query("SELECT * FROM plot_shops WHERE plot_id=?", plotId.toString())
        var level = 0
        if (rs?.next() == true) {
            level = rs.getInt("shop_level")
        }
        val shopIds = mutableListOf<UUID>()
        val rs2 = DB.query("SELECT * FROM plot_shop_locations WHERE plot_id=?", plotId.toString())
        while (rs2?.next() == true) {
            shopIds.add(UUID.fromString(rs2.getString("shop_uuid")))
        }
        return PlotShop(level, shopIds, plotType)
    }

    private suspend fun loadTotem(plotId: UUID, plotType: PlotType): PlotTotem {
        var level = 0
        var enableLightning = true
        val rs = DB.query("SELECT * FROM plot_totem_levels WHERE plot_id=?", plotId.toString())
        if (rs?.next() == true) {
            level = rs.getInt("totem_level")
            enableLightning = rs.getBoolean("enable_lightning")
        }

        val totemsRs = DB.query("SELECT * FROM plot_totems WHERE plot_id=?", plotId.toString())
        val totemList = mutableListOf<Totem>()
        while (totemsRs?.next() == true) {
            val tType = TotemType.valueOf(totemsRs.getString("totem_type"))
            val loc = totemsRs.getString("totem_location").toLocation() ?: continue
            totemList.add(Totem(tType, loc))
        }
        return PlotTotem(level, totemList, enableLightning, plotType)
    }

    private suspend fun loadNexus(plotId: UUID): MutableList<Location> {
        val nexusRs = DB.query("SELECT * FROM plot_nexus WHERE plot_id=?", plotId.toString())
        val nexusList = mutableListOf<Location>()
        while (nexusRs?.next() == true) {
            val loc = nexusRs.getString("nexus_location").toLocation() ?: continue
            nexusList.add(loc)
        }
        return nexusList
    }

    private suspend fun loadVisits(plotId: UUID, plotType: PlotType): PlotVisit {
        var visitable = true
        var level = 0
        var current = 0
        val rs = DB.query("SELECT * FROM plot_visits WHERE plot_id=?", plotId.toString())
        if (rs?.next() == true) {
            visitable = rs.getBoolean("allow_visitors")
            level = rs.getInt("visit_level")
            current = rs.getInt("current_visits")
        }
        val visitsRs = DB.query("SELECT * FROM plot_visit_timestamps WHERE plot_id=?", plotId.toString())
        val stamps = mutableListOf<Timestamp>()
        while (visitsRs?.next() == true) {
            stamps.add(visitsRs.getTimestamp("visit_timestamp"))
        }
        return PlotVisit(visitable, level, current, stamps, plotType)
    }

    suspend fun create(plot: Plot) {
        val id = plot.plotId.toString()
        DB.update("INSERT INTO plots (plot_id, type) VALUES (?,?)",
            id, plot.type.toString())
        DB.update("INSERT INTO plot_claims (plot_id, centre, home, visit) VALUES (?,?,?,?)",
            id, plot.claim.centre.fullString(false),
            plot.claim.home.fullString(), plot.claim.visit.fullString())
        DB.update("INSERT INTO plot_sizes (plot_id, size_level) VALUES (?,?)",
            id, plot.size.level)
        DB.update("INSERT INTO plot_factories (plot_id, factory_level) VALUES (?,?)",
            id, plot.factory.level)
        DB.update("INSERT INTO plot_shops (plot_id, shop_level) VALUES (?,?)",
            id, plot.shop.level)
        DB.update("INSERT INTO plot_totem_levels (plot_id, totem_level, enable_lightning) VALUES (?,?, ?)",
            id, plot.totem.level, plot.totem.enableLightning)
        DB.update("INSERT INTO plot_visits (plot_id, allow_visitors, visit_level, current_visits) VALUES (?,?,?,?)",
            id, plot.visit.visitable, plot.visit.level, plot.visit.currentVisits)
    }

    suspend fun save(plot: Plot) {
        val id = plot.plotId.toString()
        // claims
        DB.update("INSERT INTO plot_claims (plot_id, centre, home, visit) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE centre=VALUES(centre),home=VALUES(home),visit=VALUES(visit)",
            id, plot.claim.centre.fullString(false), plot.claim.home.fullString(), plot.claim.visit.fullString())
        // sizes
        DB.update("INSERT INTO plot_sizes (plot_id, size_level) VALUES (?,?) ON DUPLICATE KEY UPDATE size_level=VALUES(size_level)",
            id, plot.size.level)
        // factories
        DB.update("INSERT INTO plot_factories (plot_id, factory_level) VALUES (?,?) ON DUPLICATE KEY UPDATE factory_level=VALUES(factory_level)",
            id, plot.factory.level)
        DB.update("DELETE FROM plot_factory_locations WHERE plot_id=?", id)
        for (loc in plot.factory.factories) {
            DB.update("INSERT INTO plot_factory_locations (plot_id, factory_location) VALUES (?,?)",
                id, loc.fullString(false))
        }
        // shops
        DB.update("INSERT INTO plot_shops (plot_id, shop_level) VALUES (?,?) ON DUPLICATE KEY UPDATE shop_level=VALUES(shop_level)",
            id, plot.shop.level)
        DB.update("DELETE FROM plot_shop_locations WHERE plot_id=?", id)
        for (shop in plot.shop.getShops()) {
            DB.update("INSERT INTO plot_shop_locations (plot_id, shop_uuid) VALUES (?,?)", id, shop.toString())
        }
        // totems
        DB.update("INSERT INTO plot_totem_levels (plot_id, totem_level, enable_lightning) VALUES (?,?, ?) ON DUPLICATE KEY UPDATE totem_level=VALUES(totem_level), enable_lightning=VALUES(enable_lightning)",
            id, plot.totem.level, plot.totem.enableLightning)
        DB.update("DELETE FROM plot_totems WHERE plot_id=?", id)
        for (totem in plot.totem.totems) {
            DB.update("INSERT INTO plot_totems (plot_id, totem_type, totem_location) VALUES (?,?,?)",
                id, totem.totemType.toString(), totem.location.fullString(false))
        }

        // nexus
        DB.update("DELETE FROM plot_nexus WHERE plot_id=?", id)
        for (nexus in plot.nexus) {
            DB.update("INSERT INTO plot_nexus (plot_id, nexus_location) VALUES (?,?)", id, nexus.fullString(false))
        }

        // visits
        DB.update("INSERT INTO plot_visits (plot_id, allow_visitors, visit_level, current_visits) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE allow_visitors=VALUES(allow_visitors), visit_level=VALUES(visit_level), current_visits=VALUES(current_visits)",
            id, plot.visit.visitable, plot.visit.level, plot.visit.currentVisits)
        DB.update("DELETE FROM plot_visit_timestamps WHERE plot_id=?", id)
        for (ts in plot.visit.historicalVisits) {
            DB.update("INSERT INTO plot_visit_timestamps (plot_id, visit_timestamp) VALUES (?,?)", id, ts)
        }
    }

    suspend fun delete(plot: Plot) {
        val id = plot.plotId.toString()
        DB.update("DELETE FROM plot_visit_timestamps WHERE plot_id=?", id)
        DB.update("DELETE FROM plot_visits WHERE plot_id=?", id)
        DB.update("DELETE FROM plot_totems WHERE plot_id=?", id)
        DB.update("DELETE FROM plot_totem_levels WHERE plot_id=?", id)
        DB.update("DELETE FROM plot_shop_locations WHERE plot_id=?", id)
        DB.update("DELETE FROM plot_shops WHERE plot_id=?", id)
        DB.update("DELETE FROM plot_factory_locations WHERE plot_id=?", id)
        DB.update("DELETE FROM plot_factories WHERE plot_id=?", id)
        DB.update("DELETE FROM plot_sizes WHERE plot_id=?", id)
        DB.update("DELETE FROM plot_claims WHERE plot_id=?", id)
        DB.update("DELETE FROM plots WHERE plot_id=?", id)
        DB.update("DELETE FROM plot_nexus WHERE plot_id=?", id)
    }

    suspend fun addVisit(plot: Plot) {
        val id = plot.plotId.toString()
        val timestamp = Timestamp(System.currentTimeMillis())
        DB.update("INSERT INTO plot_visit_timestamps (plot_id, visit_timestamp) VALUES(?,?)", id, timestamp)
    }
}

//    private suspend fun loadPlotId(plotId: UUID): Plot? {
//        return asyncScope {
//            val query = """
//        SELECT * FROM plots_plots AS plots
//        INNER JOIN plots_claims AS claim ON claim.plot_id = plots.plot_id
//        INNER JOIN plots_sizes AS size ON size.plot_id = plots.plot_id
//        INNER JOIN plots_factory_limits AS factory_lim ON factory_lim.plot_id = plots.plot_id
//        INNER JOIN plots_shop_limits AS shop_lim ON shop_lim.plot_id = plots.plot_id
//        INNER JOIN plots_visitor_limits AS visitor_lim ON visitor_lim.plot_id = plots.plot_id
//        INNER JOIN plots_visit_records AS visit ON visits.plot_id = plots.plot_id
//        WHERE plots.plot_id = ?
//    """.trimIndent() // Draws complete dataset for 1 plot
//            val resultSet = DB.query(query, plotId.toString())
//            if (resultSet?.next() == true) {
//                return@asyncScope mapToPlot(resultSet)
//            }
//            else {
//                return@asyncScope null
//            }
//        }
//    }
//
//    private suspend fun loadPlotOwner(ownerId: UUID): Plot? {
//        return asyncScope {
//            val query = """
//        SELECT * FROM plots_plots AS plots
//        INNER JOIN plots_claims AS claim ON claim.plot_id = plots.plot_id
//        INNER JOIN plots_sizes AS size ON size.plot_id = plots.plot_id
//        INNER JOIN plots_factory_limits AS factory_lim ON factory_lim.plot_id = plots.plot_id
//        INNER JOIN plots_shop_limits AS shop_lim ON shop_lim.plot_id = plots.plot_id
//        INNER JOIN plots_visitor_limits AS visitor_lim ON visitor_lim.plot_id = plots.plot_id
//        INNER JOIN plots_visit_records AS visit ON visit.plot_id = plots.plot_id
//        WHERE plots.owner_id = ?
//    """.trimIndent() // Draws complete dataset for 1 plot using owner
//            val resultSet = DB.query(query, ownerId.toString())
//            if (resultSet?.next() == true) {
//                return@asyncScope mapToPlot(resultSet)
//            }
//            else {
//                return@asyncScope null
//            }
//        }
//    }
//
//    suspend fun load(plotKey: PlotKey): Plot? {
//        return if (plotKey.plotId != null) {
//            loadPlotId(plotKey.plotId)
//
//        } else if (plotKey.ownerId != null) {
//            loadPlotOwner(plotKey.ownerId)
//
//        } else {
//            null
//        }
//    }
//
//    private suspend fun mapToPlot(resultSet: ResultSet): Plot {
//        return asyncScope {
//            resultSet.next() // Get the first result (should be the only result)
//
//            val plotId = UUID.fromString(resultSet.getString("plot_id"))
//            val plotType: PlotType = PlotType.valueOf(resultSet.getString("type"))
//            val ownerId = UUID.fromString(resultSet.getString("owner_id"))
//
//            val plotClaim = PlotClaim(
//                centre = resultSet.getString("centre").toLocation()
//                    ?: throw IllegalArgumentException("Invalid centre location"), // If it fails to load, throw exception
//                home = resultSet.getString("home").toLocation()
//                    ?: throw IllegalArgumentException("Invalid home location"),
//                visit = resultSet.getString("visit").toLocation()
//                    ?: throw IllegalArgumentException("Invalid visit location")
//            )
//
//            val plotSize = PlotSize(
//                level = resultSet.getInt("size.level"),
//                plotType = plotType
//                // amount = plotConfig.getPlotSizeLevels(plotType)[resultSet.getInt("size.level")]
//
//            )
//
//            // Get list of factories
//            val factoryList = mutableListOf<Location>()
//            val subFactoriesQuerySet = DB.query("SELECT * FROM plots_factory_locations WHERE plot_id=?", plotId)
//            if (subFactoriesQuerySet != null) do { // Only execute if there are factories
//                factoryList.add(subFactoriesQuerySet.getString("factory_location").toLocation()!!) // Must be a location
//            } while (subFactoriesQuerySet.next())
//
//            val plotFactory = PlotFactory(
//                level = resultSet.getInt("factory_lim.level"),
//                factories = factoryList,
//                plotType = plotType
//            )
//
//            // Get list of shops
//            val shopList = mutableListOf<UUID>()
//            val subShopQuerySet = DB.query("SELECT * FROM plots_shop_locations WHERE plot_id=?", plotId)
//            if (subShopQuerySet != null) do { // Only execute if there are shops
//                shopList.add(UUID.fromString(subShopQuerySet.getString("shop_id"))) // Can't be null
//            } while (subShopQuerySet.next())
//            val plotShop = PlotShop(
//                level = resultSet.getInt("shop_lim.level"),
//                shops = shopList,
//                plotType = plotType
//            )
//
//            val subTotemResultSet = DB.query("SELECT * FROM plots_totems WHERE plot_id = ?", plotId)
//            val subVisitsResultSet = DB.query("SELECT * FROM plots_visit_records WHERE plot_id = ?", plotId)
//
//            val totems = mutableListOf<Totem>()
//            val visitRecords = mutableListOf<Timestamp>()
//
//            // Add all totems
//            while (subTotemResultSet?.next() == true) {
//                val totemId = subTotemResultSet.getInt("totem_id")
//                val totemType = TOTEM_TYPE.valueOf(subTotemResultSet.getString("totem_type"))
//                val totemLocation = subTotemResultSet.getString("totem_location").toLocation()
//                    ?: throw IllegalArgumentException("Invalid totem location")
//                totems.add(Totem(totemId, totemType, totemLocation))
//            }
//
//            // Add all visit timestamps
//            while (subVisitsResultSet?.next() == true) {
//                val visitTimestamp = subVisitsResultSet.getTimestamp("record")
//                visitRecords.add(visitTimestamp)
//            }
//
//            val plotTotem = PlotTotem(
//                level = resultSet.getInt("totem_level"),
//                totems = totems,
//                plotType = plotType
//            )
//
//            // Set visitor
//            val plotVisit = PlotVisit(
//                visitable = resultSet.getBoolean("visitor_lim.allow_visitors"),
//                level = resultSet.getInt("visitor_lim.level"), // Get the stored level
//                currentVisits = resultSet.getInt("visitor_lim.current_amount"),
//                historicalVisits = visitRecords,
//                plotType = plotType
//            )
//
//            return@asyncScope Plot(
//                plotId,
//                type = plotType,
//                ownerId = ownerId,
//                claim = plotClaim,
//                visit = plotVisit,
//                size = plotSize,
//                factory = plotFactory,
//                shop = plotShop,
//                totem = plotTotem
//            )
//        }
//    }
//
//    suspend fun create(plot: Plot) {
//        DB.update("INSERT INTO plots_plots (plot_id, type, owner_id) VALUES(?, ?, ?)",
//            plot.plotId,
//            plot.type.toString(),
//            plot.ownerId
//        )
//        DB.update("INSERT INTO plots_claims (centre, home, visit, plot_id) VALUES (?, ?, ?, ?, ?)",
//            plot.claim.centre.fullString(false),
//            plot.claim.home.fullString(),
//            plot.claim.visit.fullString(),
//            plot.plotId
//        )
//        // Skip totems, none placed
//        DB.update("INSERT INTO plots_sizes (level, plot_id) VALUES(?, ?)",
//            plot.size.level,
//            plot.plotId
//        )
//        DB.update("INSERT INTO plots_factory_limits (level, plot_id) VALUES(?, ?)",
//            plot.factory.level,
//            plot.plotId
//        )
//        // Skip factories placed, none placed
//        DB.update("INSERT INTO plots_shop_limits (level, plot_id) VALUES(?, ?)",
//            plot.shop.level,
//            plot.plotId
//        )
//        // Skip shops placed, none placed
//        DB.update("INSERT INTO plots_visitor_limits (level, plot_id, current_amount, allow_visitors) VALUES(?, ?, ?, ?)",
//            plot.visit.level,
//            plot.plotId,
//            plot.visit.currentVisits,
//            plot.visit.visitable
//        )
//        // No visitor records
//    }
//
//    suspend fun save(plot: Plot) {
//        // Update or insert into plots_plots
//        DB.update(
//            "INSERT INTO plots_plots (plot_id, type, owner_id) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE type = VALUES(type), owner_id = VALUES(owner_id)",
//            plot.plotId,
//            plot.type.toString(),
//            plot.ownerId
//        )
//
//        // Update or insert into plots_claims
//        DB.update(
//            "INSERT INTO plots_claims (centre, home, visit, plot_id) VALUES(?, ?, ?, ?, ?) " +
//                    "ON DUPLICATE KEY UPDATE centre = VALUES(centre), home = VALUES(home), visit = VALUES(visit)",
//            plot.claim.centre.fullString(false),
//            plot.claim.home.fullString(),
//            plot.claim.visit.fullString(),
//            plot.plotId
//        )
//
//        // Update or insert into plots_sizes
//        DB.update(
//            "INSERT INTO plots_sizes (level, plot_id) VALUES(?, ?) ON DUPLICATE KEY UPDATE level = VALUES(level)",
//            plot.size.level,
//            plot.plotId
//        )
//
//        // Update or insert into plots_factory_limits
//        DB.update(
//            "INSERT INTO plots_factory_limits (level, plot_id) VALUES(?, ?) ON DUPLICATE KEY UPDATE level = VALUES(level)",
//            plot.factory.level,
//            plot.plotId
//        )
//
//        // Update plots_factory_locations (handling list of factories)
//        for (factory in plot.factory.factories) {
//            DB.update(
//                "INSERT INTO plots_factory_locations (factory_location, plot_id) VALUES(?, ?) ON DUPLICATE KEY UPDATE plot_id = VALUES(plot_id)",
//                factory.fullString(false),
//                plot.plotId
//            )
//        }
//
//        // Update or insert into plots_shop_limits
//        DB.update(
//            "INSERT INTO plots_shop_limits (level, plot_id) VALUES(?, ?) ON DUPLICATE KEY UPDATE level = VALUES(level)",
//            plot.shop.level,
//            plot.plotId
//        )
//
//        // Update plots_shop_locations (handling list of shops)
//        for (shop in plot.shop.shops) {
//            DB.update(
//                "INSERT INTO plots_shop_locations (shop_id, plot_id) VALUES(?, ?) ON DUPLICATE KEY UPDATE plot_id = VALUES(plot_id)",
//                shop,
//                plot.plotId
//            )
//        }
//
//        // Update plots_visitor_limits
//        DB.update(
//            "INSERT INTO plots_visitor_limits (allow_visitors, level, current_amount, plot_id) VALUES(?, ?, ?, ?) " +
//                    "ON DUPLICATE KEY UPDATE allow_visitors = VALUES(allow_visitors), level = VALUES(level), current_amount = VALUES(current_amount)",
//            plot.visit.visitable,
//            plot.visit.level,
//            plot.visit.currentVisits,
//            plot.plotId
//        )
//
//        // Update plots_totems (handling list of totems)
//        for (totem in plot.totem.totems) {
//            DB.update(
//                "INSERT INTO plots_totems (totem_location, totem_type, plot_id) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE totem_type = VALUES(totem_type)",
//                totem.location.fullString(false),
//                totem.totemType.toString(),
//                plot.plotId
//            )
//        }
//    }
//
//
//    // Should hopefully never have to be called
//    suspend fun delete(plot: Plot) {
//        // Individual table deletions to honor relationships and dependencies
//        DB.update("DELETE FROM plots_visit_records WHERE plot_id = ?", plot.plotId)
//        DB.update("DELETE FROM plots_visitor_limits WHERE plot_id = ?", plot.plotId)
//        DB.update("DELETE FROM plots_shop_locations WHERE plot_id = ?", plot.plotId)
//        DB.update("DELETE FROM plots_shop_limits WHERE plot_id = ?", plot.plotId)
//        DB.update("DELETE FROM plots_factory_locations WHERE plot_id = ?", plot.plotId)
//        DB.update("DELETE FROM plots_factory_limits WHERE plot_id = ?", plot.plotId)
//        DB.update("DELETE FROM plots_totems WHERE plot_id = ?", plot.plotId)
//        DB.update("DELETE FROM plots_sizes WHERE plot_id = ?", plot.plotId)
//        DB.update("DELETE FROM plots_claims WHERE plot_id = ?", plot.plotId)
//        DB.update("DELETE FROM plots_plots WHERE plot_id = ?", plot.plotId)
//    }
//
//    suspend fun addVisit(plot: Plot) {
//        val timestamp = Timestamp(System.currentTimeMillis())
//        DB.update(
//            "INSERT INTO plots_visit_records (plot_id, record) VALUES(?, ?)",
//            plot.plotId,
//            timestamp
//        )
//    }