package dev.metronow.android.wear.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.dynamicColorScheme

/** Dark scheme derived from the phone app's brand colors (`core/presentation/ui/theme/Color.kt`). */
private val brandColorScheme = ColorScheme(
    primary = Color(0xFFAAC7FF),
    primaryDim = Color(0xFF7491C7),
    primaryContainer = Color(0xFF284777),
    onPrimary = Color(0xFF0A305F),
    onPrimaryContainer = Color(0xFFD6E3FF),
    secondary = Color(0xFFBEC6DC),
    secondaryDim = Color(0xFF8891A5),
    secondaryContainer = Color(0xFF3E4759),
    onSecondary = Color(0xFF283141),
    onSecondaryContainer = Color(0xFFDAE2F9),
    tertiary = Color(0xFFDDBCE0),
    tertiaryDim = Color(0xFFA487A9),
    tertiaryContainer = Color(0xFF573E5C),
    onTertiary = Color(0xFF3F2844),
    onTertiaryContainer = Color(0xFFFAD8FD),
    surfaceContainerLow = Color(0xFF191C20),
    surfaceContainer = Color(0xFF1D2024),
    surfaceContainerHigh = Color(0xFF282A2F),
    onSurface = Color(0xFFE2E2E9),
    onSurfaceVariant = Color(0xFFC4C6D0),
    outline = Color(0xFF8E9099),
    outlineVariant = Color(0xFF44474E),
    background = Color.Black,
    onBackground = Color(0xFFE2E2E9),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onError = Color(0xFF690005),
    onErrorContainer = Color(0xFFFFDAD6),
)

/** Uses the watch's dynamic colors when the system provides them, otherwise the brand scheme. */
@Composable
fun WearAppTheme(content: @Composable () -> Unit) {
    val colorScheme = dynamicColorScheme(LocalContext.current) ?: brandColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
