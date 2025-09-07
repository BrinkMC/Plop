package com.brinkmc.plop.plot.dto

import com.brinkmc.plop.plot.dto.modifier.PlotClaim
import com.brinkmc.plop.plot.dto.modifier.PlotFactory
import com.brinkmc.plop.plot.dto.modifier.PlotFueler
import com.brinkmc.plop.plot.dto.modifier.PlotNexus
import com.brinkmc.plop.plot.dto.modifier.PlotOwner
import com.brinkmc.plop.plot.dto.modifier.PlotShop
import com.brinkmc.plop.plot.dto.modifier.PlotSize
import com.brinkmc.plop.plot.dto.modifier.PlotTotem
import com.brinkmc.plop.plot.dto.modifier.PlotVisit
import com.brinkmc.plop.plot.constant.PlotType
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

data class Plot(
    val id: UUID,
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
}