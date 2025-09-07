package com.brinkmc.plop.factory

import com.brinkmc.plop.Plop
import com.brinkmc.plop.factory.service.FactoryActionService
import com.brinkmc.plop.factory.service.FactoryAugmentService
import com.brinkmc.plop.factory.service.FactoryService
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State

class Factories(override val plugin: Plop): Addon, State {

    override val factoryService = FactoryService(plugin)
    override val factoryActionService = FactoryActionService(plugin)
    override val factoryAugmentService = FactoryAugmentService(plugin)

    override suspend fun load() {
        listOf(
            factoryService,
            factoryActionService,
            factoryAugmentService
        ).forEach { handler -> (handler as State).load() }
    }

    override suspend fun kill() {
        TODO("Not yet implemented")
    }

}