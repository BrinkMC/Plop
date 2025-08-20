package com.brinkmc.plop.factory.storage.database

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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.bukkit.Location
import java.sql.Timestamp
import java.util.UUID

class DatabaseFactory(override val plugin: Plop): Addon, State {

    private val mutex = Mutex()

    override suspend fun load() {}

    override suspend fun kill() {}

    suspend fun load(plotId: UUID): Plot? = mutex.withLock {
        val id = plotId
        val plot = loadPlotCore(id) ?: return null
        plot.setClaim(loadClaim(id) ?: plot.claim)
        plot.setSize(loadSize(id) ?: plot.size)
        plot.setFactory(loadFactory(id, plot.type))
        plot.setShop(loadShop(id, plot.type))
        plot.setTotem(loadTotem(id, plot.type))
        plot.setVisit(loadVisits(id, plot.type))
        plot.addNexus(loadNexus(id))
        return plot
    }

    private suspend fun loadPlotCore(plotId: UUID): Plot? {
        val rs = DB.query("SELECT * FROM plots WHERE plot_id=?", plotId.toString()) ?: return null
        return if (rs.next()) {
            Plot(
                plotId = plotId,
                type = PlotType.valueOf(rs.getString("type")),
                _nexus = mutableListOf(),
                _claim = PlotClaim(Location(null, 0.0, 0.0, 0.0), Location(null,0.0,0.0,0.0), Location(null,0.0,0.0,0.0)),
                _visit = PlotVisit(true,0,0, mutableListOf(),PlotType.PERSONAL),
                _size = PlotSize(0,PlotType.PERSONAL),
                _factory = PlotFactory(0, mutableListOf(),PlotType.PERSONAL),
                _shop = PlotShop(0,PlotType.PERSONAL),
                _totem = PlotTotem(0, mutableListOf(),true,PlotType.PERSONAL)
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
        return PlotShop(level, plotType)
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

    suspend fun create(plot: Plot) = mutex.withLock {
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

    suspend fun save(unsafe: Plot) = mutex.withLock {
        val plot = unsafe.getSnapshot()

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

    suspend fun delete(plot: Plot) = mutex.withLock {
        val id = plot.plotId.toString()
        DB.update("DELETE FROM plot_visit_timestamps WHERE plot_id=?", id)
        DB.update("DELETE FROM plot_visits WHERE plot_id=?", id)
        DB.update("DELETE FROM plot_totems WHERE plot_id=?", id)
        DB.update("DELETE FROM plot_totem_levels WHERE plot_id=?", id)
        DB.update("DELETE FROM plot_shops WHERE plot_id=?", id)
        DB.update("DELETE FROM plot_factory_locations WHERE plot_id=?", id)
        DB.update("DELETE FROM plot_factories WHERE plot_id=?", id)
        DB.update("DELETE FROM plot_sizes WHERE plot_id=?", id)
        DB.update("DELETE FROM plot_claims WHERE plot_id=?", id)
        DB.update("DELETE FROM plots WHERE plot_id=?", id)
        DB.update("DELETE FROM plot_nexus WHERE plot_id=?", id)
    }

    suspend fun addVisit(plot: Plot) = mutex.withLock {
        val id = plot.plotId.toString()
        val timestamp = Timestamp(System.currentTimeMillis())
        DB.update("INSERT INTO plot_visit_timestamps (plot_id, visit_timestamp) VALUES(?,?)", id, timestamp)
    }
}