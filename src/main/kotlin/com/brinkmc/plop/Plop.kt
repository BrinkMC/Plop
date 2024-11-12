package com.brinkmc.plop

import com.brinkmc.plop.plot.Plots
import com.brinkmc.plop.shop.Shops
import org.bukkit.plugin.java.JavaPlugin


class Plop : JavaPlugin() {
    private val plugin = this


    lateinit var plots: Plots
    lateinit var shops: Shops

    override fun onEnable() {

        plots = Plots(plugin)
        shops = Shops(plugin)
    }

    override fun onDisable() {

    }

}