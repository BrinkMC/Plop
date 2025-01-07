package com.brinkmc.plop.plot.preview

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import java.util.UUID

class PreviewInstance(override val plugin: Plop, val player: UUID): Addon {

    lateinit var previewPlot: OpenPlot

}