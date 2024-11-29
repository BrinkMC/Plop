package com.brinkmc.plop.shared.display

import com.brinkmc.plop.shared.base.State
import java.util.*

internal interface DisplayTracker: State {
    override fun kill() {
        TODO("Not yet implemented")
    }

    override fun load() {
        TODO("Not yet implemented")
    }

    val primaryTracking: MutableList<Runnable>
    val secondaryTracking: MutableList<Runnable>

    val renderList: MutableList<UUID>


    fun addPlayerTrack(uuid: UUID) {
        renderList.add(uuid)
    }

    fun removePlayerTrack(uuid: UUID) {
        renderList.remove(uuid)
    }

    fun clearPlayerTracks() {
        renderList.clear()
    }

    fun secondaryTrackLoop(uuid: UUID) {

    }

    fun primaryTrackLoop(uuid: UUID) {

    }

}