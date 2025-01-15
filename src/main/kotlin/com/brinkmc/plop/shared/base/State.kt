package com.brinkmc.plop.shared.base

internal interface State {

    /** Contains logic for when this is enabled */
    suspend fun load()

    /** Contains logic for when this is disabled */
    suspend fun kill()
}