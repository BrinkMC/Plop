package com.brinkmc.plop.factory.factory

import com.brinkmc.plop.shared.util.factory.FactoryRotation
import com.brinkmc.plop.shared.util.factory.FactoryType
import java.util.UUID



data class Factory(
    val id: UUID,

    private val _factoryType: FactoryType,
    private val _rotation: FactoryRotation


)
