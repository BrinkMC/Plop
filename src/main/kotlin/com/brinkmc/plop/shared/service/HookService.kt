package com.brinkmc.plop.shared.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.hook.api.FancyHologram
import com.brinkmc.plop.shared.hook.api.Economy
import com.brinkmc.plop.shared.hook.api.Guilds
import com.brinkmc.plop.shared.hook.api.MythicMobs
import com.brinkmc.plop.shared.hook.api.PacketEvents
import com.brinkmc.plop.shared.hook.api.PlayerTracker
import com.brinkmc.plop.shared.hook.api.WorldGuard
import com.brinkmc.plop.shared.hook.listener.DamageListener
import com.brinkmc.plop.shared.hook.listener.GeneralListener
import com.brinkmc.plop.shared.hook.listener.GuildListener
import com.brinkmc.plop.shared.hook.listener.NexusListener
import com.brinkmc.plop.shared.hook.listener.PreviewListener
import com.brinkmc.plop.shared.hook.listener.ShopListener
import com.brinkmc.plop.shared.hook.listener.TotemListener
import com.brinkmc.plop.shared.hook.listener.VisitListener
import com.github.retrooper.packetevents.event.PacketListenerPriority
import com.noxcrew.interfaces.InterfacesListeners

class HookService(val plugin: Plop): State {
    val display = FancyHologram(plugin)
    val guilds = Guilds(plugin)
    val packetEvents = PacketEvents(plugin)
    val mythicMobs = MythicMobs(plugin)
    val worldGuard = WorldGuard(plugin)
    val economy = Economy(plugin)

    lateinit var damageListener: DamageListener
    lateinit var generalListener: GeneralListener
    lateinit var guildListener: GuildListener
    lateinit var nexusListener: NexusListener
    lateinit var playerTracker: PlayerTracker
    lateinit var previewListener: PreviewListener
    lateinit var shopListener: ShopListener
    lateinit var totemListener: TotemListener
    lateinit var visitListener: VisitListener

    override suspend fun load() {
        listOf(
            guilds,
            display,
            mythicMobs,
            worldGuard,
            economy
        ).forEach { hook -> hook.load() }
        com.github.retrooper.packetevents.PacketEvents.getAPI().init()
        com.github.retrooper.packetevents.PacketEvents.getAPI().eventManager.registerListener(packetEvents, PacketListenerPriority.NORMAL)

        InterfacesListeners.Companion.install(plugin)

        plugin.slF4JLogger.info("Initiating listeners")
        listOf(
            damageListener,
            generalListener,
            guildListener,
            nexusListener,
            playerTracker,
            previewListener,
            shopListener,
            totemListener,
            visitListener
        ).forEach { listener ->
            plugin.server.pluginManager.registerEvents(listener, plugin)
            listener.load()
        }
    }

    override suspend fun kill() {
        listOf(
            guilds,
            display,
            mythicMobs,
            worldGuard,
            economy
        ).forEach { hook -> hook.kill() }

        listOf(
            damageListener,
            generalListener,
            guildListener,
            nexusListener,
            playerTracker,
            previewListener,
            shopListener,
            totemListener,
            visitListener
        ).forEach { listener -> listener.kill() }
    }
}