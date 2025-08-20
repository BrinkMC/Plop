package com.brinkmc.plop.factory.factory

import com.brinkmc.plop.plot.plot.base.PlotType
import java.util.UUID

enum class FactoryType { // Types of shop
    PLANTER,
    PLACER,
    BREAKER,
    REFINER,
    SPAWNER
}

data class Factory(
    val factoryId: UUID,
    val plotId: UUID,
    private var _factoryType: FactoryType,
    private var _location: String,

)
