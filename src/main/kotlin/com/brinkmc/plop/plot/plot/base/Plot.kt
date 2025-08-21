package com.brinkmc.plop.plot.plot.base

import com.brinkmc.plop.plot.plot.modifier.PlotClaim
import com.brinkmc.plop.plot.plot.modifier.PlotFactory
import com.brinkmc.plop.plot.plot.modifier.PlotFueler
import com.brinkmc.plop.plot.plot.modifier.PlotNexus
import com.brinkmc.plop.plot.plot.modifier.PlotOwner
import com.brinkmc.plop.plot.plot.modifier.PlotSize
import com.brinkmc.plop.plot.plot.modifier.PlotShop
import com.brinkmc.plop.plot.plot.modifier.PlotTotem
import com.brinkmc.plop.plot.plot.modifier.PlotVisit
import com.brinkmc.plop.shared.util.plot.PlotType
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.glaremasters.guilds.Guilds
import org.bukkit.Bukkit
import org.bukkit.Location
import java.util.*

/*
I find best practice for data classes tends to be separate the logic into extension functions.
This works well in keeping the data class itself relatively clean
 */

data class Plot(
    val plotId: UUID,
    val type: PlotType,

    // Sub modules
    private var _claim: PlotClaim,
    private var _visit: PlotVisit,
    private var _size: PlotSize,
    private var _factory: PlotFactory,
    private var _shop: PlotShop,
    private var _totem: PlotTotem,
    private var _fueler: PlotFueler,
    private var _nexus: PlotNexus,
    private var _owner: PlotOwner,
) {
    private val mutex = Mutex()

    // Thread-safe getters
    val nexus: PlotNexus get() = _nexus
    val claim: PlotClaim get() = _claim
    val visit: PlotVisit get() = _visit
    val size: PlotSize get() = _size
    val factory: PlotFactory get() = _factory
    val shop: PlotShop get() = _shop
    val totem: PlotTotem get() = _totem
    val fueler: PlotFueler get() = _fueler
    val owner: PlotOwner get() = _owner

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
}
