package dev.metronow.android.features.infotext.domain

import androidx.compose.ui.graphics.Color
import java.time.LocalDateTime

data class InfoText (
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
        fun toColor(): Color {
            return when(this) {
                HIGH -> Color.Red
                NORMAL -> Color(0xFF007EFF)
                LOW -> Color(0xFF00C604)
            }
        }
    }
}
