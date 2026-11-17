package dev.metronow.android.features.infotext.presentation

import androidx.compose.ui.graphics.Color
import dev.metronow.android.features.infotext.domain.InfoText

fun InfoText.Priority.toColor(): Color = when (this) {
    InfoText.Priority.HIGH -> Color.Red
    InfoText.Priority.NORMAL -> Color(0xFF007EFF)
    InfoText.Priority.LOW -> Color(0xFF00C604)
}
