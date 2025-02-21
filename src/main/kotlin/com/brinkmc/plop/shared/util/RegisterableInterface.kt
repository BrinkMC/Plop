package com.brinkmc.plop.shared.util

import com.noxcrew.interfaces.interfaces.Interface

// Thank you NoxCrew for the code

interface RegistrableInterface {
    suspend fun create(vararg args: Any): Interface<*, *>
}