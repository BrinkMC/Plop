package com.brinkmc.plop.shop

import org.bukkit.Location
import org.bukkit.entity.Item
import java.util.UUID

data class Shop (
    // Primary
    val id: UUID,

    val location: Location,
    val creator: UUID, // Original placer

    // Mutable
    var ware: Item,
    var stock: Int,

    var price: Float

) {
    fun addMember() {
        //TODO
    }

    fun removeMember() {
        //TODO
    }
}

