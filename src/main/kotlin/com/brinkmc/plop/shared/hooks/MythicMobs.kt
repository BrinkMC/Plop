package com.brinkmc.plop.shared.hooks

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import io.lumine.mythic.api.mobs.MythicMob
import io.lumine.mythic.bukkit.MythicBukkit
import me.glaremasters.guilds.api.GuildsAPI

class MythicMobs(override val plugin: Plop): Addon, State {

    private lateinit var mythicAPI:  MythicBukkit

    override suspend fun load() {
        mythicAPI = MythicBukkit.inst()
    }

    override suspend fun kill() {
        TODO("Not yet implemented")
    }
}