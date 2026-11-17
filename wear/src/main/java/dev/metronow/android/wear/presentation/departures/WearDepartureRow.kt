package dev.metronow.android.wear.presentation.departures

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import dev.metronow.android.core.presentation.secondsToString
import dev.metronow.android.features.departure.domain.DepartureWithCountdown
import dev.metronow.android.features.departure.domain.RouteDetail
import dev.metronow.android.features.departure.presentation.toColor

/**
 * Compact departure row: route badge, one-line headsign and the first countdown.
 * Unlike the phone's `DepartureItem` it has no "Also in" line or second headsign.
 */
@Composable
internal fun WearDepartureRow(
    routeDetail: RouteDetail?,
    departure: DepartureWithCountdown,
    modifier: Modifier = Modifier,
    transformation: SurfaceTransformation? = null,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
        transformation = transformation,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = routeDetail?.name ?: "?",
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(color = routeDetail?.toColor() ?: Color.Black)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
            )
            Text(
                text = departure.heading.orEmpty(),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = departure.countdown.secondsToString(),
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
            )
        }
    }
}
