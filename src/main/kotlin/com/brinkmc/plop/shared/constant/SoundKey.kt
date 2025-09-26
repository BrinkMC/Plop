package com.brinkmc.plop.shared.constant

import net.kyori.adventure.key.Key

enum class SoundKey(val key: Key) {
    // General
    SUCCESS(Key.key("ENTITY_VILLAGER_YES")),
    FAILURE(Key.key("ENTITY_VILLAGER_NO")),
    BACK(Key.key("UI_BUTTON_BACK")),
    CLICK(Key.key("UI_BUTTON_CLICK")),
    RECEIVE_ITEM(Key.key("ENTITY_ITEM_PICKUP")),
    // Plot specific
    UPGRADE(Key.key("UI_TOAST_CHALLENGE_COMPLETE")),
    TELEPORT(Key.key("ENTITY_ENDERMAN_TELEPORT")),
    // Shop specific
    OPEN(Key.key("BLOCK_CHEST_OPEN")),
    CLOSE(Key.key("BLOCK_CHEST_CLOSE")),
    CLOSED(Key.key("BLOCK_CHEST_LOCKED")), // When shop is closed
    BUY(Key.key("BLOCK_AMETHYST_BLOCK_RESONATE")),
    SELL(Key.key("BLOCK_AMETHYST_BLOCK_RESONATE")),
}