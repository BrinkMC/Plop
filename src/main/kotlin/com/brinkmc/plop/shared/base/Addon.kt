package com.brinkmc.plop.shared.base

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.Plots
import com.brinkmc.plop.plot.handler.PlotUpgradeHandler
import com.brinkmc.plop.shared.config.ConfigReader
import com.brinkmc.plop.shared.storage.HikariManager
import com.brinkmc.plop.shared.util.MessageService
import com.brinkmc.plop.shop.Shops
import org.bukkit.NamespacedKey
import org.bukkit.Server
import org.bukkit.entity.Player

internal interface Addon {

    val plugin: Plop

    val server: Server
        get() = plugin.server

    val logger: org.slf4j.Logger
        get() = plugin.slF4JLogger

    val DB: HikariManager
        get() = plugin.DB

    val plots: Plots
        get() = plugin.plots

    val shops: Shops
        get() = plugin.shops

    val configManager: ConfigReader
        get() = plugin.getConfigManager()

    val lang: MessageService
        get() = plugin.getMessageService()

    // Provide an easy way to get formatted MiniMessage messages with custom tags also replaced properly
    fun Player.sendFormattedMessage(message: String) {
        with (lang) { sendFormattedMessage(message) }
    }


}