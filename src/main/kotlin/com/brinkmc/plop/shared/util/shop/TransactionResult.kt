package com.brinkmc.plop.shared.util.shop

enum class TransactionResult {
    SUCCESS,
    PLAYER_INSUFFICIENT_STOCK,
    PLAYER_INSUFFICIENT_BALANCE,
    SHOP_INSUFFICIENT_STOCK,
    SHOP_INSUFFICIENT_BALANCE,
    BUY_LIMIT_REACHED,
    FAILURE
}