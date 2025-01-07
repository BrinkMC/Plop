package com.brinkmc.plop.plot.handler

import com.brinkmc.plop.plot.preview.PreviewInstance
import java.util.UUID

/*
Keep track of all active preview instances
Ensure that data is saved e.t.c
 */

class PlotPreviewHandler {

    val previews = mutableListOf<PreviewInstance>()

    // All functions available to player
    fun startPreview(player: UUID) {

    }

    fun endPreview(player: UUID) {

    }

    fun claimPlot(player: UUID){

    }

    fun nextPlot(player: UUID) {

    }

    fun previousPlot(player: UUID) {

    }

}