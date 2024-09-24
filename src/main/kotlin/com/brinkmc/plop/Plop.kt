package com.brinkmc.plop

import com.brinkmc.plop.plot.Plots
import org.bukkit.plugin.java.JavaPlugin


class Plop : JavaPlugin() {
    private val plugin = this


    lateinit var plots: Plots

    override fun onEnable() {

        plots = Plots(plugin)

    }

    override fun onDisable() {

    }

}