package com.brinkmc.plop.shared.storage

import java.util.Objects
import java.util.UUID

/*
Plot key so that I can find things fast in O(1) time

Plot can either have
(plotId & ownerId) or (plotId & guildId)
 */
data class PlotKey(
    val plotId: UUID? = null,
    val ownerId: UUID? = null
) {
    // Literally overrides the == sign for PlotKeys lol
    override fun equals(other: Any?): Boolean {
        if (this === other) return true // Check if both references point to the same object
        if (javaClass != other?.javaClass) return false // Check if the object is the same as PlotKey
        other as PlotKey // Cast to PlotKey because it's the same, and we can treat it as such
        return ( plotId == other.plotId || ownerId == other.ownerId ) // Finally compare if the two are the same
    }

    override fun hashCode(): Int {
        return Objects.hash(plotId, ownerId) // Create hash from all 2 things put together
    }
}

class Cache {

}