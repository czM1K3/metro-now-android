package dev.metronow.android.features.infotext.domain

import dev.metronow.android.core.data.AppLanguage
import java.time.LocalDateTime

data class InfoText(
    val id: String,
    val textCz: String,
    val textEn: String?,
    val validFrom: LocalDateTime?,
    val validTo: LocalDateTime?,
    val priority: Priority,
) {
    enum class Priority {
        LOW,
        NORMAL,
        HIGH
    }

    fun getCorrectText() = when (AppLanguage.getCurrent()) {
        AppLanguage.ENGLISH -> textEn ?: textCz
        AppLanguage.CZECH -> textCz
    }
}
