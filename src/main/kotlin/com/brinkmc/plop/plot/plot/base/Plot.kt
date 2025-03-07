package com.brinkmc.plop.plot.plot.base

import com.brinkmc.plop.plot.plot.modifier.PlotClaim
import com.brinkmc.plop.plot.plot.modifier.PlotFactory
import com.brinkmc.plop.plot.plot.modifier.PlotSize
import com.brinkmc.plop.plot.plot.modifier.PlotShop
import com.brinkmc.plop.plot.plot.modifier.PlotTotem
import com.brinkmc.plop.plot.plot.modifier.PlotVisit
import com.brinkmc.plop.shared.hooks.Locals.localWorld
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.glaremasters.guilds.Guilds
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import java.util.*

/*
I find best practice for data classes tends to be separate the logic into extension functions.
This works well in keeping the data class itself relatively clean
 */

enum class PlotType {
    PERSONAL,
    GUILD
}

data class Plot(
    val plotId: UUID,
    val type: PlotType,
    private val _nexus: MutableList<Location> = mutableListOf(),
    private var _claim: PlotClaim,
    private var _visit: PlotVisit,
    private var _size: PlotSize,
    private var _factory: PlotFactory,
    private var _shop: PlotShop,
    private var _totem: PlotTotem
) {
    private val mutex = Mutex()

    // Thread-safe getters
    val nexus: List<Location> get() = _nexus.toList()
    val claim: PlotClaim get() = _claim
    val visit: PlotVisit get() = _visit
    val size: PlotSize get() = _size
    val factory: PlotFactory get() = _factory
    val shop: PlotShop get() = _shop
    val totem: PlotTotem get() = _totem

    // Thread-safe setters
    suspend fun setClaim(claim: PlotClaim) = mutex.withLock {
        _claim = claim
    }

    suspend fun setVisit(visit: PlotVisit) = mutex.withLock {
        _visit = visit
    }

    suspend fun setSize(size: PlotSize) = mutex.withLock {
        _size = size
    }

    suspend fun setFactory(factory: PlotFactory) = mutex.withLock {
        _factory = factory
    }

    suspend fun setShop(shop: PlotShop) = mutex.withLock {
        _shop = shop
    }

    suspend fun setTotem(totem: PlotTotem) = mutex.withLock {
        _totem = totem
    }

    // Thread-safe nexus operations
    suspend fun addNexus(location: Location) = mutex.withLock {
        _nexus.add(location)
    }

    suspend fun addNexus(location: List<Location>) = mutex.withLock {
        _nexus.addAll(location)
    }

    suspend fun removeNexus(location: Location) = mutex.withLock {
        _nexus.remove(location)
    }

    suspend fun clearNexus() = mutex.withLock {
        _nexus.clear()
    }

    // Thread-safe snapshot
    suspend fun getSnapshot(): Plot = mutex.withLock {
        copy(
            _nexus = ArrayList(_nexus),
            _claim = _claim.copy(),
            _visit = _visit.copy(historicalVisits = ArrayList(_visit.historicalVisits)),
            _size = _size.copy(),
            _factory = _factory.copy(factories = ArrayList(_factory.factories)),
            _shop = _shop.copy(),
            _totem = _totem.copy(totems = ArrayList(_totem.totems))
        )
    }

    // Get the owner of the plot
    val owner: PlotOwner by lazy {
        if (type == PlotType.GUILD) {
            val guild = Guilds.getApi().getGuild(plotId) // Try to find guild

            if (guild != null) {
                PlotOwner.GuildOwner(guild)
            } else {
                throw IllegalStateException("Guild not found for plot $plotId")
            }
        } else {
            PlotOwner.PlayerOwner(Bukkit.getOfflinePlayer(plotId))
        }
    }
}
