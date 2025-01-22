package com.brinkmc.plop.shared.hooks

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.api.GuildsAPI

class Guilds(override val plugin: Plop): Addon, State {

    lateinit var guildAPI: GuildsAPI // Guild API

    override suspend fun load() {
        guildAPI = Guilds.getApi()
    }

    override suspend fun kill() {
        TODO("Not yet implemented")
    }
}