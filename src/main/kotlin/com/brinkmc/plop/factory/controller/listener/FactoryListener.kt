package com.brinkmc.plop.factory.controller.listener

import com.brinkmc.plop.Plop
import com.brinkmc.plop.factory.constant.FactoryType
import com.brinkmc.plop.factory.dao.DatabaseFactory
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.constant.MessageKey
import com.brinkmc.plop.shared.constant.ServiceResult
import com.brinkmc.plop.shared.constant.SoundKey
import com.brinkmc.plop.shared.item.enum.TrackedItemKey
import io.lumine.mythic.bukkit.events.MythicPlayerAttackEvent
import io.papermc.paper.event.block.BlockPreDispenseEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDispenseEvent
import org.bukkit.event.block.BlockPlaceEvent

class FactoryListener(override val plugin: Plop): State, Addon {

    override suspend fun load() {
    }

    override suspend fun kill() {
    }

    @EventHandler
    suspend fun onBlockDispenseEvent(event: BlockPreDispenseEvent) {
        val factoryId = factoryService.getFactoryIdFromLocation(event.block.location) ?: return

        // we now have our factory, therefore cancel what default behaviour is
        event.isCancelled = true

        // Get necessary details to process the factory
        val dispenser = event.block
        val itemStack = event.itemStack
        val facing = event.block.getFace(dispenser) ?: return

        val confirmation = factoryActionService.factoryActivation(factoryId, dispenser, facing, itemStack)

        when (confirmation) {
            is ServiceResult.Failure -> {
                event.isCancelled = false
            }
            is ServiceResult.Success -> {
                // Nothing is required, should just work
            }
        }
        return
    }

    @EventHandler
    suspend fun onBlockPlaceEvent(event: BlockPlaceEvent) {
        // Not interested if not an item which can be a factory
        val factory = itemService.getTrackingType(event.itemInHand) ?: return

        if (factory !is FactoryType) {
            return
        }

        val playerId = event.player.uniqueId

        when (val result = factoryService.placeFactory(playerId, event.blockPlaced.location, factory)) {
            is ServiceResult.Failure -> {
                event.isCancelled = true
                messages.resolveFailure(playerId, result)
            }
            is ServiceResult.Success -> {
                messages.resolveSuccess(playerId, result)
            }
        }
    }

    @EventHandler
    suspend fun onBlockBreakEvent(event: BlockBreakEvent) {
        val factoryId = factoryService.getFactoryIdFromLocation(event.block.location) ?: return
        event.isCancelled = true // It is a factory

        val playerId = event.player.uniqueId

        when (val result = factoryService.destroyFactory(factoryId)) {
            is ServiceResult.Failure -> {
                event.isCancelled = false // Something went wrong so let them break it
                messages.resolveFailure(playerId, result)
            }
            is ServiceResult.Success -> {
                messages.resolveSuccess(playerId, result)
            }
        }
    }
}