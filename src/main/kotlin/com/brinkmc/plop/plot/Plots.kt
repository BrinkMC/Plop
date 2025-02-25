package com.brinkmc.plop.plot

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.handler.*
import com.brinkmc.plop.plot.nexus.NexusManager
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.plot.plot.modifier.PlotTotem
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
    lateinit var totemHandler: PlotTotemHandler
    lateinit var previewHandler: PlotPreviewHandler
    lateinit var factoryHandler: PlotFactoryHandler
    lateinit var claimHandler: PlotClaimHandler
    lateinit var shopHandler: PlotShopHandler
    lateinit var sizeHandler: PlotSizeHandler

    lateinit var nexusManager: NexusManager

    override suspend fun load() {
        logger.info("Loading Plots...")

        // Create worlds on main thread
        syncScope {

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
        totemHandler = PlotTotemHandler(plugin)
        factoryHandler = PlotFactoryHandler(plugin)
        claimHandler = PlotClaimHandler(plugin)
        shopHandler = PlotShopHandler(plugin)
        sizeHandler = PlotSizeHandler(plugin)
        nexusManager = NexusManager(plugin)

        listOf(
            handler,
            visitorHandler,
            upgradeHandler,
            previewHandler,
            totemHandler,
            factoryHandler,
            claimHandler,
            shopHandler,
            sizeHandler,
            nexusManager
        ).forEach { handler -> (handler as State).load() }
    }

    override suspend fun kill() {
        listOf(
            handler,
            visitorHandler,
            upgradeHandler,
            previewHandler,
            totemHandler,
            factoryHandler,
            claimHandler,
            shopHandler,
            sizeHandler,
            nexusManager
        ).forEach { handler -> (handler as State).kill() }
    }


}