package dev.metronow.android.features.infotext.data.db

import dev.metronow.android.features.infotext.domain.InfoText
import dev.metronow.android.features.infotext.domain.InfoText.Priority.*
import java.time.LocalDateTime

fun DbInfoText.toDomain(): InfoText {
    return InfoText(
        id = id,
        textCz = textCz,
        textEn = textEn,
        validTo = validTo?.let { LocalDateTime.parse(it) },
        validFrom = validFrom?.let { LocalDateTime.parse(it) },
        priority = when (priority) {
            "HIGH" -> HIGH
            "LOW" -> LOW
            else -> NORMAL
        }
    )
}

fun InfoText.toDb(): DbInfoText {
    return DbInfoText(
        id = id,
        textCz = textCz,
        textEn = textEn,
        validTo = validTo?.toString(),
        validFrom = validFrom?.toString(),
        priority = when (priority) {
            LOW -> "LOW"
            NORMAL -> "NORMAL"
            HIGH -> "HIGH"
        }
    )
}
