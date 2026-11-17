package dev.metronow.android.wear.presentation

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import dev.metronow.android.wear.presentation.departures.WearDeparturesScreen
import dev.metronow.android.wear.presentation.permission.WearLocationPermission

/** Wear Compose Navigation has no type-safe routes, so destinations are plain strings. */
private object WearRoutes {
    const val DEPARTURES = "departures"
}

@Composable
fun WearNavigation() {
    val navController = rememberSwipeDismissableNavController()
    AppScaffold {
        SwipeDismissableNavHost(
            navController = navController,
            startDestination = WearRoutes.DEPARTURES,
        ) {
            composable(WearRoutes.DEPARTURES) {
                WearLocationPermission {
                    WearDeparturesScreen()
                }
            }
        }
    }
}
