package dev.metronow.android.features.infotext.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import coil.compose.AsyncImage
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import dev.metronow.android.R
import dev.metronow.android.core.data.AppLanguage
import dev.metronow.android.features.infotext.domain.InfoText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoTextDetailScreen(
    goBack: () -> Unit,
    vIewModel: InfoTextDetailVIewModel = koinViewModel(),
) {
    val screenState by vIewModel.screenStateStream.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = goBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.go_back),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            )
        },
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) { paddingValues ->
        Box(
            modifier = Modifier.padding(paddingValues),
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                screenState.infoText?.let { infoText ->
                    Text(
                        text = infoText.getCorrectText(),
                        style = TextStyle.Default.copy(
                            lineBreak = LineBreak.Paragraph,
                            fontSize = 4.em,
                        )
                    )
                    HorizontalDivider(
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                    infoText.validFrom?.let {
                        Text(
                            text = stringResource(
                                R.string.valid_from,
                                it.customString()
                            )
                        )
                    }
                    infoText.validTo?.let {
                        Text(
                            text = stringResource(
                                R.string.valid_to,
                                it.customString()
                            )
                        )
                    }
                    Row {
                        Text(stringResource(R.string.priority))
                        Text(
                            text = when (infoText.priority) {
                                InfoText.Priority.HIGH -> stringResource(R.string.high)
                                InfoText.Priority.NORMAL -> stringResource(R.string.normal)
                                InfoText.Priority.LOW -> stringResource(R.string.low)
                            },
                            color = infoText.priority.toColor()
                        )
                    }
                    Row (
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        AsyncImage(
                            model = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQM3gRUA9_EYg40rdgLKsj7M-NSw-4wAQVifg&s",
                            contentDescription = stringResource(R.string.pid_logo),
                        )
                    }
                }
            }
        }
    }
}

private fun LocalDateTime.customString(): String {
    val formatString = AppLanguage.getCurrent().getFormat()
    val format = DateTimeFormatter.ofPattern(formatString)
    return this.format(format)
}
