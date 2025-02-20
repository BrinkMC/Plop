package com.brinkmc.plop.shop

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.PlotOwner
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import me.glaremasters.guilds.guild.Guild
import org.bukkit.entity.Player
import kotlin.math.log

class Shops(override val plugin: Plop): Addon,State {

    val shopList: MutableList<Shop> = mutableListOf()

    override suspend fun load() {
        logger.info("Loading shops...")
    }

    override suspend fun kill() {
        TODO("Not yet implemented")
    }

    suspend fun Player.personalShops(): List<Shop> {
        // One-liner to return all shops found on a player plot
        return shopList.filter { shop -> (plots.handler.getPlotFromLocation(shop.location)?.owner as PlotOwner.PlayerOwner).player.uniqueId == this.uniqueId }
    }

    suspend fun Guild.guildShops(): List<Shop> {
        // One-liner to return all shops found on a guild plot
        return shopList.filter { shop -> (plots.handler.getPlotFromLocation(shop.location)?.owner as PlotOwner.GuildOwner).guild?.id == this.id }
    }
}