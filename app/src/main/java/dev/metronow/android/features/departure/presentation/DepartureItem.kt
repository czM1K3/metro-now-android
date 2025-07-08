package dev.metronow.android.features.departure.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.metronow.android.core.presentation.secondsToString
import dev.metronow.android.features.departure.domain.DepartureWithCountdown
import dev.metronow.android.features.departure.domain.RouteDetail
import dev.metronow.android.R

@Composable
fun DepartureItem(
    modifier: Modifier = Modifier,
    routeDetail: RouteDetail?,
    departures: List<DepartureWithCountdown>,
) {
    val heading = departures.firstOrNull()?.heading
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Text(
                text = routeDetail?.name ?: "?",
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(color = routeDetail?.getColor() ?: Color.Black)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                color = Color.White,
            )
            Column (
                modifier = Modifier.padding(start = 4.dp)
            ) {
                heading?.let {
                    Text(it)
                }
                departures.elementAtOrNull(1)?.heading?.let { secondHeading ->
                    if (secondHeading != heading) {
                        Text(
                            text = secondHeading,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = TextStyle(
                                fontSize = 12.sp,
                            )
                        )
                    }
                }
            }
        }
        Column(
            horizontalAlignment = Alignment.End,
        ) {
            departures.firstOrNull()?.let {
                Text(
                    text = it.countdown.secondsToString()
                )
            }
            departures.elementAtOrNull(1)?.let {
                Text(
                    text = stringResource(R.string.also_in, it.countdown.secondsToString()),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = TextStyle(
                        fontSize = 12.sp,
                    ),
                )
            }
        }
    }
}
