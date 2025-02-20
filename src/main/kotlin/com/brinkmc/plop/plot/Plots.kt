package com.brinkmc.plop.plot

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.handler.PlotClaimHandler
import com.brinkmc.plop.plot.handler.PlotFactoryHandler
import com.brinkmc.plop.plot.handler.PlotHandler
import com.brinkmc.plop.plot.handler.PlotPreviewHandler
import com.brinkmc.plop.plot.handler.PlotShopHandler
import com.brinkmc.plop.plot.handler.PlotSizeHandler
import com.brinkmc.plop.plot.handler.PlotUpgradeHandler
import com.brinkmc.plop.plot.handler.PlotVisitHandler
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.generator.ChunkGenerator

class Plots(override val plugin: Plop): Addon, State {

    // Sub handlers
    lateinit var handler: PlotHandler
    lateinit var visitorHandler: PlotVisitHandler
    lateinit var upgradeHandler: PlotUpgradeHandler
    lateinit var previewHandler: PlotPreviewHandler
    lateinit var factoryHandler: PlotFactoryHandler
    lateinit var claimHandler: PlotClaimHandler
    lateinit var shopHandler: PlotShopHandler
    lateinit var sizeHandler: PlotSizeHandler

    override suspend fun load() {
        logger.info("Loading Plots...")
        // Create worlds on main thread
        plugin.sync {

            val personalWorldName = plotConfig.getPlotWorld(PlotType.PERSONAL)
            val guildWorldName = plotConfig.getPlotWorld(PlotType.GUILD)

            val personalWorldGen = plotConfig.getPlotWorldGenerator(PlotType.PERSONAL)
            val guildWorldGen = plotConfig.getPlotWorldGenerator(PlotType.GUILD)

            server.createWorld(
                WorldCreator(personalWorldName)
                    .environment(World.Environment.NORMAL)
                    .generator(personalWorldGen)
            )

            server.createWorld(
                WorldCreator(guildWorldName)
                    .environment(World.Environment.NORMAL)
                    .generator(guildWorldGen)
            )

            // Register / load worlds on server
        }

        handler = PlotHandler(plugin)
        visitorHandler = PlotVisitHandler(plugin)
        upgradeHandler = PlotUpgradeHandler(plugin)
        previewHandler = PlotPreviewHandler(plugin)
        factoryHandler = PlotFactoryHandler(plugin)
        claimHandler = PlotClaimHandler(plugin)
        shopHandler = PlotShopHandler(plugin)
        sizeHandler = PlotSizeHandler(plugin)

        listOf(
            handler,
            visitorHandler,
            upgradeHandler,
            previewHandler,
            factoryHandler,
            claimHandler,
            shopHandler,
            sizeHandler
        ).forEach { handler -> (handler as State).load() }
    }

    override suspend fun kill() {
        TODO("Not yet implemented")
    }


}