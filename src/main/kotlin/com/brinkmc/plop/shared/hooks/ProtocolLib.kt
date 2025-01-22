package com.brinkmc.plop.shared.hooks

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State

class ProtocolLib(override val plugin: Plop): Addon, State {
    override suspend fun load() {
        plotConfig
    }

    override suspend fun kill() {
        TODO("Not yet implemented")
    }
}