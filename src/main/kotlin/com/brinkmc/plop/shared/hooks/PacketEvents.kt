package com.brinkmc.plop.shared.hooks

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.event.PacketListener
import com.github.retrooper.packetevents.event.PacketReceiveEvent
import com.github.retrooper.packetevents.event.PacketSendEvent
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder

class PacketEvents(override val plugin: Plop): Addon, PacketListener {

    override fun onPacketReceive(event: PacketReceiveEvent?) {
        super.onPacketReceive(event)
    }

    override fun onPacketSend(event: PacketSendEvent?) {
        super.onPacketSend(event)
    }
}