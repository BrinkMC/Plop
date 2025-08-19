package com.brinkmc.plop.shared.util.message

import org.bukkit.Sound

enum class SoundKey(val sound: Sound) {
    // General
    SUCCESS(Sound.ENTITY_VILLAGER_YES),
    FAILURE(Sound.ENTITY_VILLAGER_NO),
    BACK(Sound.BLOCK_BAMBOO_WOOD_DOOR_CLOSE),
    CLICK(Sound.UI_BUTTON_CLICK),
    RECEIVE_ITEM(Sound.ENTITY_ITEM_PICKUP),
    // Plot specific
    UPGRADE(Sound.UI_TOAST_CHALLENGE_COMPLETE),
    TELEPORT(Sound.ENTITY_ENDERMAN_TELEPORT),
    // Shop specific
    OPEN(Sound.BLOCK_CHEST_OPEN),
    CLOSE(Sound.BLOCK_CHEST_CLOSE),
    CLOSED(Sound.BLOCK_CHEST_LOCKED), // When shop is closed
    BUY(Sound.BLOCK_AMETHYST_BLOCK_RESONATE),
    SELL(Sound.BLOCK_AMETHYST_BLOCK_RESONATE),
}