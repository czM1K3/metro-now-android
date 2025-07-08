package dev.metronow.android.core.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.metronow.android.R

@Composable
fun CustomCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    isMetro: Boolean = false,
    state: CustomCardState,
    content: @Composable () -> Unit = {},
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            disabledContentColor = MaterialTheme.colorScheme.onSurface,
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
        ) {
            if (title != null) {
                Text(
                    text = title,
                    fontSize = 24.sp,
                )
            }
            if (isMetro) {
                Text(
                    text = stringResource(R.string.metro),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            when (state) {
                CustomCardState.Empty -> {
                    Text(
                        text = stringResource(R.string.empty),
                    )
                }

                CustomCardState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.then(Modifier.size(24.dp)),
                        strokeWidth = 3.dp,
                    )
                }

                CustomCardState.Showing -> {
                    content()
                }
            }
        }
    }
}

enum class CustomCardState {
    Empty,
    Loading,
    Showing,
}
