package com.brinkmc.plop.shop

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import org.bukkit.entity.Player

class Shops(override val plugin: Plop): Addon,State {

    val shopList: MutableList<Shop> = mutableListOf()

    override fun load() {
        TODO("Not yet implemented")
    }

    override fun kill() {
        TODO("Not yet implemented")
    }

    fun Player.shops(): List<Shop> {
        // One-liner to return only guild plots, which have the player as a member or the player as the leader
        return shopList.filter { shop -> plots.getPlot(shop.plot).owner == player?.uniqueId }
    }
}