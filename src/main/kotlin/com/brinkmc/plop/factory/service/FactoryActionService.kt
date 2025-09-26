package com.brinkmc.plop.factory.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State

class FactoryActionService(override val plugin: Plop): Addon, State {
    override suspend fun load() {
        // No-op
    }

    override suspend fun kill() {
        // No-op
    }
}