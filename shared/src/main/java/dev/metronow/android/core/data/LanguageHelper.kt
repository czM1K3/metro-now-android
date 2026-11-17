package dev.metronow.android.core.data

import androidx.core.os.LocaleListCompat

enum class AppLanguage {
    ENGLISH,
    CZECH;

    fun getFormat() = when (this) {
        ENGLISH -> "yyyy-MM-dd HH:mm"
        CZECH -> "dd.MM.yyyy HH:mm"
    }

    companion object {
        fun getCurrent(): AppLanguage {
            val locale = LocaleListCompat.getDefault().get(0)
            return when (locale?.language) {
                "cs" -> CZECH
                else -> ENGLISH
            }
        }
    }
}