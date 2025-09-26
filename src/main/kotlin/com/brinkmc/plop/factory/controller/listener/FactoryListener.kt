package com.brinkmc.plop.factory.controller.listener

import com.brinkmc.plop.Plop
import com.brinkmc.plop.factory.dao.DatabaseFactory
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State

class FactoryListener(override val plugin: Plop): State, Addon {

    override suspend fun load() {
    }

    override suspend fun kill() {
    }

}