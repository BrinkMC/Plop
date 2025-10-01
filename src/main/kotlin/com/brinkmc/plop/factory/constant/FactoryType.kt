package com.brinkmc.plop.factory.constant

import com.brinkmc.plop.shared.item.enum.TrackedItemKey
import io.papermc.paper.registry.tag.TagKey
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.BlockType


enum class FactoryType(val tags: List<Tag<Material>>, val material: List<Material> = emptyList(), val exclude: List<Material> = emptyList()): TrackedItemKey { // Types of shop
    PLANTER(
        listOf(
            Tag.FLOWER_POTS,
            Tag.CROPS,
            )
    ),

    BREAKER(
        listOf(
            Tag.MINEABLE_PICKAXE,
            Tag.MINEABLE_AXE
        )
    ),

    HARVESTER(
        listOf(
            Tag.CROPS,
            Tag.LOGS,
            ),
        listOf(
            Material.MELON, // This is the block, often MELON_BLOCK in older versions
            Material.PUMPKIN
            ),
        listOf()
    ),

    PLACER(
        listOf(
            Tag.MINEABLE_PICKAXE,
            Tag.MINEABLE_AXE,
            Tag.MINEABLE_SHOVEL
        ),
    ),

    STRIPPER(
        listOf(
            Tag.LOGS
        )
    )
}


//enum class FactoryType(blockTypes: List<BlockType>? = null, tagList: List<Tag>? = null): TrackedItemKey { // Types of shop
//    PLANTER(listOf(
//        BlockType.WHEAT,
//        BlockType.BEETROOTS,
//        BlockType.CARROTS,
//        BlockType.CHERRY_SAPLING,
//        BlockType.MELON_STEM,
//        BlockType.PUMPKIN_STEM,
//        BlockType.TORCHFLOWER_CROP,
//        BlockType.PITCHER_CROP,
//        BlockType.BAMBOO_SAPLING,
//        BlockType.COCOA,
//        BlockType.SUGAR_CANE,
//        BlockType.SWEET_BERRY_BUSH,
//        BlockType.CACTUS,
//        BlockType.RED_MUSHROOM,
//        BlockType.BROWN_MUSHROOM,
//        BlockType.KELP,
//        BlockType.SEA_PICKLE,
//        BlockType.NETHER_WART,
//        BlockType.CRIMSON_FUNGUS,
//        BlockType.WARPED_FUNGUS,
//        BlockType.CHORUS_FLOWER,
//        BlockType.OAK_SAPLING,
//        BlockType.SPRUCE_SAPLING,
//        BlockType.BIRCH_SAPLING,
//        BlockType.JUNGLE_SAPLING,
//        BlockType.ACACIA_SAPLING,
//        BlockType.DARK_OAK_SAPLING,
//        BlockType.CHERRY_SAPLING,
//        BlockType.PALE_OAK_SAPLING,
//        BlockType.AZALEA,
//        BlockType.MANGROVE_PROPAGULE,
//    )),
//    BREAKER(listOf(
//        BlockType.WHEAT,
//        BlockType.BEETROOTS,
//        BlockType.CARROTS,
//        BlockType.POTATOES,
//        BlockType.MELON,
//        BlockType.PUMPKIN,
//        BlockType.TORCHFLOWER,
//        BlockType.PITCHER_PLANT,
//        BlockType.BAMBOO,
//        BlockType.COCOA,
//        BlockType.SUGAR_CANE,
//        BlockType.SWEET_BERRY_BUSH, // Don't break block
//        BlockType.CACTUS,
//        BlockType.RED_MUSHROOM_BLOCK,
//        BlockType.BROWN_MUSHROOM_BLOCK,
//        BlockType.MUSHROOM_STEM,
//        BlockType.KELP,
//        BlockType.SEA_PICKLE,
//        BlockType.NETHER_WART,
//        BlockType.CHORUS_PLANT,
//        // Trees
//        BlockType.OAK_LOG,
//        BlockType.SPRUCE_LOG,
//        BlockType.BIRCH_LOG,
//        BlockType.JUNGLE_LOG,
//        BlockType.ACACIA_LOG,
//        BlockType.DARK_OAK_LOG,
//        BlockType.CHERRY_LOG,
//        BlockType.PALE_OAK_LOG,
//        BlockType.MANGROVE_LOG,
//        // Dirts
//        BlockType.DIRT,
//        BlockType.GRASS_BLOCK,
//        BlockType.COARSE_DIRT,
//        BlockType.PODZOL,
//        BlockType.MYCELIUM,
//        BlockType.MOSS_BLOCK,
//        BlockType.ROOTED_DIRT,
//        BlockType.FARMLAND,
//        BlockType.MUD,
//        BlockType.CLAY,
//        BlockType.SAND,
//        BlockType.RED_SAND,
//        BlockType.GRAVEL,
//        BlockType.SOUL_SAND,
//        BlockType.SOUL_SOIL,
//        BlockType.NETHERRACK,
//        BlockType.CRIMSON_NYLIUM,
//        BlockType.WARPED_NYLIUM,
//        BlockType.STONE,
//        BlockType.COBBLESTONE,
//        BlockType.SANDSTONE,
//        BlockType.RED_SANDSTONE,
//        BlockType.ICE,
//        BlockType.PACKED_ICE,
//        BlockType.BLUE_ICE,
//        BlockType.SNOW_BLOCK,
//        BlockType.OBSIDIAN,
//        BlockType.END_STONE,
//        BlockType.BASALT,
//
//    ),
//        listOf(
//            Tag.LOGS.values,
//        )
//    ),
//    PLACER(listOf(
//
//    )),
//    STRIPPER(listOf(
//
//    )),
//
//}