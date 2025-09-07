package com.brinkmc.plop.plot

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.service.*
import com.brinkmc.plop.plot.service.PlotNexusService
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import org.bukkit.World
import org.bukkit.WorldCreator

class Plots(override val plugin: Plop): Addon, State {

    // Sub Services
    override val plotService = PlotService(plugin)
    override val plotVisitService = PlotVisitService(plugin)
    override val plotUpgradeService = PlotUpgradeService(plugin)
    override val plotTotemService = PlotTotemService(plugin)
    override val plotPreviewService = PlotPreviewService(plugin)
    override val plotFactoryService = PlotFactoryService(plugin)
    override val plotClaimService = PlotClaimService(plugin)
    override val plotShopService = PlotShopService(plugin)
    override val plotSizeService = PlotSizeService(plugin)
    override val plotFuelerService = PlotFuelerService(plugin)
    override val plotNexusService = PlotNexusService(plugin)
    override val plotLayoutService = PlotLayoutService(plugin)

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

        plotService = PlotService(plugin)
        visitorService = PlotVisitService(plugin)
        upgradeService = PlotUpgradeService(plugin)
        previewService = PlotPreviewService(plugin)
        totemService = PlotTotemService(plugin)
        factoryService = PlotFactoryService(plugin)
        claimService = PlotClaimService(plugin)
        shopService = PlotShopService(plugin)
        sizeService = PlotSizeService(plugin)
        nexusService = PlotNexusService(plugin)
        fuelerService = PlotFuelerService(plugin)

        listOf(
            plotService,
            visitorService,
            upgradeService,
            previewService,
            totemService,
            factoryService,
            claimService,
            shopService,
            sizeService,
            nexusService,
            fuelerService
        ).forEach { Service -> (Service as State).load() }
    }

    override suspend fun kill() {
        listOf(
            plotService,
            visitorService,
            upgradeService,
            previewService,
            totemService,
            factoryService,
            claimService,
            shopService,
            sizeService,
            nexusService,
            fuelerService
        ).forEach { Service -> (Service as State).kill() }
    }


}