package com.brinkmc.plop.shop.shop

import com.brinkmc.plop.shared.util.shop.ShopType
import java.sql.Timestamp
import java.util.UUID

data class ShopTransaction(
    private val timestamp: Timestamp, // Primary key
    private val id: UUID,
    private val amount: Int,
    private val type: ShopType,
)
