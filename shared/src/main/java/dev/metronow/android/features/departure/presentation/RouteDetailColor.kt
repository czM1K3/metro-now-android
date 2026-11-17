package dev.metronow.android.features.departure.presentation

import androidx.compose.ui.graphics.Color
import dev.metronow.android.features.departure.domain.RouteDetail
import dev.metronow.android.features.departure.domain.VehicleType

fun RouteDetail.toColor(): Color {
    if (isNight) return Color.Black
    if (isSubstitute) return Color.Red
    return when (vehicleType) {
        VehicleType.BUS -> Color.Blue
        VehicleType.TRAM -> Color(0xFF0985FC)
        VehicleType.FERRY -> Color.Cyan
        VehicleType.TRAIN -> Color.Gray
        VehicleType.FUNICULAR -> Color(0xFF924900)
        VehicleType.SUBWAY -> when (name) {
            "A" -> Color(0xFF00A551)
            "B" -> Color(0xFFFECB09)
            else -> Color(0xFFEC1F25)
        }
        VehicleType.TROLLEYBUS -> Color(0xFFFE6520)
    }
}
