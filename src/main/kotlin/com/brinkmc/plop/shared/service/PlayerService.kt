package com.brinkmc.plop.shared.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.dto.Plot
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.asLoadingCache
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID

class PlayerService(override val plugin: Plop): Addon, State {

    private val locations = Caffeine.newBuilder().asLoadingCache<Player, UUID?> {
        plotService.getPlotIdFromLocation(it.location)
    }

    suspend fun getPlotId(player: Player): UUID? {
        return locations.get(player)
    }

    fun clearCache(player: Player) {
        locations.invalidate(player)
    }

    fun getPlayer(playerId: UUID): Player? {
        return Bukkit.getPlayer(playerId)
    }


    override suspend fun load() { }

    override suspend fun kill() {
        locations.invalidateAll()
    }


}