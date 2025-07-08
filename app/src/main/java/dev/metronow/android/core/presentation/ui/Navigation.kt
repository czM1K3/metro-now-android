package dev.metronow.android.core.presentation.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dev.metronow.android.features.infotext.presentation.detail.InfoTextDetailScreen
import dev.metronow.android.features.infotext.presentation.list.InfoTextsListScreen
import dev.metronow.android.features.location.presentation.LocationNotAvailable
import dev.metronow.android.features.location.presentation.LocationPermission
import dev.metronow.android.features.stop.presentation.StopsScreen
import dev.metronow.android.R

import kotlinx.serialization.Serializable

sealed interface Screens {
    @Serializable
    data object ClosestStop : Screens

    @Serializable
    data object InfoTexts : Screens

    @Serializable
    data class InfoText(val id: String): Screens
}

@Composable
fun Navigation() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Screens.ClosestStop,
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right) },
        exitTransition = { ExitTransition.None },
    ) {
        composable<Screens.ClosestStop> {
            LocationPermission(
                rationale = stringResource(R.string.rationale),
                permissionNotAvailableContent = {
                    LocationNotAvailable()
                }
            ) {
                StopsScreen(navigateToInfoTextsList = {
                    navController.navigate(Screens.InfoTexts)
                })
            }
        }
        composable<Screens.InfoTexts> {
            InfoTextsListScreen(
                goBack = {
                    navController.popBackStack()
                },
                infoTextDetail = { id ->
                    navController.navigate(Screens.InfoText(id))
                }
            )
        }
        composable<Screens.InfoText> {
            InfoTextDetailScreen(
                goBack = {
                    navController.popBackStack()
                },
            )
        }
    }
}
