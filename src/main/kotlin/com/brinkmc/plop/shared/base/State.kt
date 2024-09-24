package com.brinkmc.plop.shared.base

internal interface State {

    /** Contains logic for when this is enabled */
    fun load()

    /** Contains logic for when this is disabled */
    fun kill()
}