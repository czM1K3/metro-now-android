package dev.metronow.android.core.presentation

import kotlin.math.abs

fun Double.secondsToString(): String {
    val num = abs(this)
    val hours = (num / 3600).toLong()
    val minutes = (num % 3600 / 60).toLong()
    val seconds = (num % 60).toLong()
    val isNegative = this < 0
    var res = if(isNegative) "-" else ""
    if (hours > 0) {
        res += "${hours}h ${minutes}m"
    } else if (minutes > 0) {
        res += "${minutes}m ${seconds}s"
    } else {
        res += "${seconds}s"
    }
    return res
}
