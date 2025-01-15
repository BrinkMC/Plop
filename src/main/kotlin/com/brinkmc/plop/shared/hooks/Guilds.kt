package com.brinkmc.plop.shared.hooks

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.api.GuildsAPI

class Guilds(override val plugin: Plop): Addon, State {

    lateinit var guildAPI: GuildsAPI // Guild API

    override fun load() {
        guildAPI = Guilds.getApi()
    }

    override fun kill() {
        TODO("Not yet implemented")
    }


}