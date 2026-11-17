package dev.metronow.android.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dev.metronow.android.wear.presentation.WearNavigation
import dev.metronow.android.wear.presentation.theme.WearAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearAppTheme {
                WearNavigation()
            }
        }
    }
}
