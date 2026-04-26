package com.brinkmc.plop.shared.constant

sealed class ServiceResult {
    data class Success(
        val translatable: Translatable? = null,
        val soundKey: SoundKey? = null,
        val action: (() -> Unit)? = null
    ) : ServiceResult()

    data class Failure(
        val translatable: Translatable? = null,
        val soundKey: SoundKey? = null,
        val action: (() -> Unit)? = null
    ) : ServiceResult()
}