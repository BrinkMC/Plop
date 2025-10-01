package com.brinkmc.plop.shared.constant

sealed class ServiceResult {
    data class Success(
        val messageKey: MessageKey? = null,
        val soundKey: SoundKey? = null,
        val action: (() -> Unit)? = null
    ) : ServiceResult()

    data class Failure(
        val messageKey: MessageKey? = null,
        val soundKey: SoundKey? = null,
        val action: (() -> Unit)? = null
    ) : ServiceResult()
}