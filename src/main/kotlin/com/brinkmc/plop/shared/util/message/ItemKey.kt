package com.brinkmc.plop.shared.util.message

import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.inventory.ItemStack

enum class ItemKey(val item: ItemStack) {
    // General
    GOOD(ItemStack(Material.EMERALD)),
    BAD(ItemStack(Material.GRAY_STAINED_GLASS_PANE)),
    BACK(ItemStack(Material.REDSTONE)),

    MORE(ItemStack(Material.GOLD_INGOT)),
    LESS(ItemStack(Material.GOLD_NUGGET)),

    STOCK(ItemStack(Material.BARREL)),
    LOGS(ItemStack(Material.WRITTEN_BOOK)),

    BUY(ItemStack(Material.HOPPER)),
    SELL(ItemStack(Material.CHEST)),

    RESTOCK(ItemStack(Material.VAULT)),
    CLICK_ENABLE(ItemStack(Material.NETHER_STAR)),
    CLOSE_SHOP(ItemStack(Material.IRON_DOOR)),
    OPEN_SHOP(ItemStack(Material.OAK_DOOR)),
    DELETE_SHOP(ItemStack(Material.BARRIER)),

    // Creation
    CREATE_CHOOSE(ItemStack(Material.BOOK)),
}