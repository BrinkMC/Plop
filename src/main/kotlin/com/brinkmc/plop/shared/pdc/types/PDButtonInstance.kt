package com.brinkmc.plop.shared.pdc.types

import java.util.UUID



data class PDButtonInstance(
    val player: UUID,
    val buttonType: String
)