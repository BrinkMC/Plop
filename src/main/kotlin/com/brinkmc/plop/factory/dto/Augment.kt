package com.brinkmc.plop.factory.dto

import com.brinkmc.plop.factory.constant.AugmentType
import java.util.UUID

data class Augment(
    private val _id: UUID, // Mythic ID to identify the specific item
    private val _augmentType: AugmentType
) {
    val id: UUID get() = _id
    val augmentType: AugmentType get() = _augmentType
}