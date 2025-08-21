package com.brinkmc.plop.shared.hook.api

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import io.lumine.mythic.bukkit.MythicBukkit

class MythicMobs(override val plugin: Plop): Addon, State {

    private lateinit var mythicAPI:  MythicBukkit

    override suspend fun load() {
        mythicAPI = MythicBukkit.inst()
    }

    override suspend fun kill() {
        TODO("Not yet implemented")
    }
}