package com.brinkmc.plop.factory.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.factory.constant.FactoryType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.constant.ServiceResult
import com.brinkmc.plop.shared.util.CoroutineUtils.async
import com.brinkmc.plop.shared.util.CoroutineUtils.sync
import com.brinkmc.plop.shared.util.type.MutexSet
import com.github.shynixn.mccoroutine.bukkit.ticks
import kotlinx.coroutines.delay
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.BlockType
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.Directional
import org.bukkit.block.data.Rotatable
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import java.util.UUID
import kotlin.math.roundToInt
import kotlin.text.clear
import kotlin.text.set

class FactoryActionService(override val plugin: Plop): Addon, State {

    private val planterCache = mutableMapOf<BlockType, BlockData>()

    private val dispenserLock = MutexSet<Location>()

    override suspend fun load() {
        populatePlanterCache()
    }

    override suspend fun kill() {
        planterCache.clear()
    }

    private fun populatePlanterCache() {
        val validBlockTypes = parseTypeAsBlockType(FactoryType.PLANTER)
        for (blockType in validBlockTypes) {
            val blockData = blockType.createBlockData()
            planterCache[blockType] = blockData
        }
    }

    private fun getPlanterSeeds(): List<Material> {
        return planterCache.values.map { it.placementMaterial }
    }

    private fun getPlanterBlockData(material: Material): BlockData? {
        return planterCache[material.asBlockType()]?.clone()
    }



    suspend fun factoryActivation(factoryId: UUID, dispenser: Block, facing: BlockFace, itemStack: ItemStack): ServiceResult {
        val factoryType = factoryService.getFactoryType(factoryId) ?: return ServiceResult.Failure()
        return when (factoryType) {
            FactoryType.PLANTER -> planterFactory(factoryId,dispenser, facing, itemStack)
            FactoryType.HARVESTER -> harvesterFactory(factoryId,dispenser, facing, itemStack)
            FactoryType.PLACER -> placerFactory(factoryId,dispenser, facing, itemStack)
            FactoryType.BREAKER -> breakerFactory(factoryId,dispenser, facing, itemStack)
            FactoryType.STRIPPER -> stripperFactory(factoryId,dispenser, facing, itemStack)
        }
    }

    private suspend fun planterFactory(factoryId: UUID, block: Block, facing: BlockFace, itemStack: ItemStack): ServiceResult {
        val validSeeds = getPlanterSeeds()
        val plantBlock = block.location.add(facing.direction)

        if (itemStack.type !in validSeeds) {
            return ServiceResult.Failure()
        } // Not a seed

        val toPlant = getPlanterBlockData(itemStack.type) ?: return ServiceResult.Failure()


        if (!toPlant.isSupported(plantBlock)) { // Can't place plant here, wouldn't be possible in vanilla
            return ServiceResult.Failure()
        }

        // Plant time
        plantBlock.world.setBlockData(
            plantBlock,
            toPlant
        )

        val inventory = (block.state as org.bukkit.block.Dispenser).inventory

        decrementInventoryByMaterial(inventory, itemStack)

        return ServiceResult.Success()
    }

    private suspend fun harvesterFactory(factoryId: UUID, block: Block, facing: BlockFace,  itemStack: ItemStack): ServiceResult {
        val blockBeingDestroyed = block.location.add(facing.direction).block
        val destroyableList = parseTypeAsMaterial(FactoryType.HARVESTER)

        if (dispenserLock.contains(block.location)) {
            return ServiceResult.Failure()
        }

        if (blockBeingDestroyed.type !in destroyableList) {
            return ServiceResult.Failure()
        }
        // In list to destroy
        if (!blockBeingDestroyed.isPreferredTool(itemStack)) {
            return ServiceResult.Failure()
        }

        if (decrementDurability(itemStack) == null) {
            return ServiceResult.Failure() // Broke
        }

        // Can destroy block
        val destroySpeed = blockBeingDestroyed.getDestroySpeed(itemStack)
        val hardness = blockBeingDestroyed.type.hardness
        val timeTaken = 1 / (destroySpeed / hardness / 30) // in ticks

        // By this point we schedule async task to break the block because we don't want to keep the dispenser locked up
        plugin.async {
            dispenserLock.add(block.location)
            plugin.sync {
                blockBeingDestroyed.breakNaturally(itemStack, true, false)
            }
            delay(timeTaken.roundToInt().ticks) // Time till I try break block
            // Unlock the dispenser
            dispenserLock.remove(block.location)
        }
        return ServiceResult.Success()
    }

    private suspend fun breakerFactory(factoryId: UUID, block: Block, facing: BlockFace,  itemStack: ItemStack): ServiceResult {
        val blockBeingDestroyed = block.location.add(facing.direction).block
        val destroyableList = parseTypeAsMaterial(FactoryType.BREAKER)

        if (dispenserLock.contains(block.location)) {
            return ServiceResult.Failure()
        }

        if (blockBeingDestroyed.type !in destroyableList) {
            return ServiceResult.Failure()
        }
        // In list to destroy
        if (!blockBeingDestroyed.isPreferredTool(itemStack)) {
            return ServiceResult.Failure()
        }

        // Can destroy block
        val destroySpeed = blockBeingDestroyed.getDestroySpeed(itemStack)
        val hardness = blockBeingDestroyed.type.hardness
        val timeTaken = 1 / (destroySpeed / hardness / 30) // in ticks

        if (decrementDurability(itemStack) == null) {
            return ServiceResult.Failure() // Broke
        }

        // By this point we schedule async task to break the block because we don't want to keep the dispenser locked up
        plugin.async {
            dispenserLock.add(block.location)
            plugin.sync {
                blockBeingDestroyed.breakNaturally(itemStack, true, false)
            }
            delay(timeTaken.roundToInt().ticks) // Time till I try break block
            // Unlock the dispenser
            dispenserLock.remove(block.location)
        }
        return ServiceResult.Success()
    }

    private suspend fun placerFactory(factoryId: UUID, block: Block, facing: BlockFace,  itemStack: ItemStack): ServiceResult {
        val blockToPlace = block.location.add(facing.direction).block
        if (!blockToPlace.type.isAir) {
            return ServiceResult.Failure()
        } // Can't place on non-air

        if (!itemStack.type.isBlock) {
            return ServiceResult.Failure()
        } // Can't place non-blocks

        val placeableBlocks = parseTypeAsMaterial(FactoryType.PLACER)
        if (itemStack.type !in placeableBlocks) {
            return ServiceResult.Failure()
        } // Not a placeable block

        // Place
        plugin.syncScope {
            blockToPlace.type = itemStack.type
            // Orientation
            when (val blockData = blockToPlace.blockData) {
                is Directional -> {
                    blockData.facing = facing
                    blockToPlace.blockData = blockData
                }
                is Rotatable -> {
                    blockData.rotation = facing
                    blockToPlace.blockData = blockData
                }
            }
        }

        decrementInventoryByMaterial((block.state as org.bukkit.block.Dispenser).inventory, itemStack)
        return ServiceResult.Success()
    }

    private suspend fun stripperFactory(factoryId: UUID, block: Block, facing: BlockFace,  itemStack: ItemStack): ServiceResult {
        val blockBeingStripped = block.location.add(facing.direction).block
        val stripableList = parseTypeAsMaterial(FactoryType.STRIPPER)

        // No dispenser lock needed, as stripping is instant and doesn't drop items
        if (blockBeingStripped.type !in stripableList) {
            return ServiceResult.Failure()
        }
        // In list to destroy
        if (!blockBeingStripped.isPreferredTool(itemStack)) {
            return ServiceResult.Failure()
        }
        if (decrementDurability(itemStack) == null) {
            return ServiceResult.Failure() // Broke
        }
        val strippedItem = ItemStack(Material.valueOf("STRIPPED_" + blockBeingStripped.type.name))

        // Set block to stripped version
        plugin.syncScope {
            blockBeingStripped.type = strippedItem.type
        }
        return ServiceResult.Success()
    }

    private suspend fun decrementInventoryByMaterial(inv: Inventory, stack: ItemStack): Boolean {
        val slot = inv.first(stack.type)
        if (slot < 0) return false

        if (stack.amount == 1) {
            plugin.syncScope {
                delay(1.ticks)
                inv.contents[slot]?.let { it.amount -= 1 }
            }
            return true
        }

        inv.contents[slot]?.let { it.amount -= 1 } ?: return false
        return true
    }

    private fun decrementDurability(itemStack: ItemStack): ItemStack? {
        if (!itemStack.type.isItem) return itemStack
        if (itemStack.itemMeta?.isUnbreakable == true) return itemStack
        val livingEntity = plugin.getFirstLivingEntity() ?: throw RuntimeException("Nothing lives!!!???? WHAT")

        if (itemStack.itemMeta !is Damageable) return itemStack
        val damageable = itemStack.itemMeta as Damageable
        if (damageable.damage <= 1) {
            return null // Broke so return null to get it dispensed instead of damaged
        }

        return itemStack.damage(1, livingEntity)
    }

    private fun parseTypeAsBlockType(factoryType: FactoryType): List<BlockType> {
        return parseTypeAsMaterial(factoryType).mapNotNull { material -> material.asBlockType() }
    }

    private fun parseTypeAsMaterial(factoryType: FactoryType): List<Material> {
        val validTags = factoryType.tags
        val additionalMaterials = factoryType.material
        val excludeMaterials = factoryType.exclude

        val allMaterials = mutableListOf<Material>()

        for (tag in validTags) {
            allMaterials.addAll(tag.values.toList())
        }

        allMaterials.addAll(additionalMaterials)
        allMaterials.removeAll(excludeMaterials)

        return allMaterials
    }
}