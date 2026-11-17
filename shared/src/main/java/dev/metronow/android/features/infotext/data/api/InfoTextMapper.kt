package dev.metronow.android.features.infotext.data.api

import dev.metronow.android.InfoTextsQuery
import dev.metronow.android.features.infotext.domain.InfoText
import dev.metronow.android.type.InfotextPriority
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun InfoTextsQuery.Infotext.toDomain(): InfoText {
    return InfoText(
        id = id,
        textEn = textEn,
        textCz = text,
        validFrom = validFrom?.let { LocalDateTime.parse(it as String, DateTimeFormatter.ISO_DATE_TIME) },
        validTo = validTo?.let { LocalDateTime.parse(it as String, DateTimeFormatter.ISO_DATE_TIME) },
        priority = when (priority) {
            InfotextPriority.HIGH -> InfoText.Priority.HIGH
            InfotextPriority.NORMAL -> InfoText.Priority.NORMAL
            InfotextPriority.LOW -> InfoText.Priority.LOW
            InfotextPriority.UNKNOWN__ -> InfoText.Priority.NORMAL
        }
    )
}
