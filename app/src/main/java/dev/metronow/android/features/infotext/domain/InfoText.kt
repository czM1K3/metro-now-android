package dev.metronow.android.features.infotext.domain

import androidx.compose.ui.graphics.Color
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
        HIGH;

        fun toColor() = when (this) {
            HIGH -> Color.Red
            NORMAL -> Color(0xFF007EFF)
            LOW -> Color(0xFF00C604)
        }
    }

    fun getCorrectText() = when (AppLanguage.getCurrent()) {
        AppLanguage.ENGLISH -> textEn ?: textCz
        AppLanguage.CZECH -> textCz
    }
}
