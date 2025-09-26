package com.brinkmc.plop.shop.dto

import com.brinkmc.plop.shop.constant.ShopType
import java.sql.Timestamp
import java.util.UUID

data class ShopTransaction(
    val timestamp: Timestamp, // Primary key
    val playerId: UUID,
    val amount: Int,
    val cost: Double,
)
