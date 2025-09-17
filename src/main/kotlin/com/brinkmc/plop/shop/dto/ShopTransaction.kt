package com.brinkmc.plop.shop.dto

import com.brinkmc.plop.shop.constant.ShopType
import java.sql.Timestamp
import java.util.UUID

data class ShopTransaction(
    private val timestamp: Timestamp, // Primary key
    private val playerId: UUID,
    private val amount: Int,
    private val cost: Double,
)
