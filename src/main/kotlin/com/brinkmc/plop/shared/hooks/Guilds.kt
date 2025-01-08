package com.brinkmc.plop.shared.hooks

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State

class Guilds(override val plugin: Plop): Addon, State {

    val guildAPI = Guilds.getAPI() // Guild API



}