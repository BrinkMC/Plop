package com.brinkmc.plop.shared.util

import com.google.gson.Gson
import java.util.UUID
import kotlin.reflect.KProperty

data class PersistentDataInterpreter(
    val player: UUID,
    var buttonName: String
) {
    fun toJson(): String {
        return Gson().toJson(this)
    }
}