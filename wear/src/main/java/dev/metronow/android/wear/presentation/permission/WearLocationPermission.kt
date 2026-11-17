package dev.metronow.android.wear.presentation.permission

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dev.metronow.android.shared.R
import dev.metronow.android.wear.presentation.theme.WearAppTheme

/**
 * Shows [content] once both location permissions are granted.
 *
 * Same decision logic as the phone's `LocationPermission`:
 * - when a rationale should be shown, the button asks again;
 * - otherwise the request is launched once (first launch, or the system won't ask any more) and
 *   the button opens the app's system settings page.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WearLocationPermission(content: @Composable () -> Unit) {
    val permissionState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    )

    if (permissionState.allPermissionsGranted) {
        content()
        return
    }

    val context = LocalContext.current
    if (permissionState.shouldShowRationale) {
        PermissionDeniedContent(
            buttonText = stringResource(R.string.ok),
            onClick = { permissionState.launchMultiplePermissionRequest() },
        )
    } else {
        // Either the first request, or the user declined twice and the system won't ask again
        LaunchedEffect(Unit) {
            permissionState.launchMultiplePermissionRequest()
        }
        PermissionDeniedContent(
            buttonText = stringResource(R.string.go_to_settings),
            onClick = {
                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                )
            },
        )
    }
}

@Composable
private fun PermissionDeniedContent(
    buttonText: String,
    onClick: () -> Unit,
) {
    val listState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()
    ScreenScaffold(scrollState = listState) { contentPadding ->
        TransformingLazyColumn(
            state = listState,
            contentPadding = contentPadding,
        ) {
            item {
                ListHeader(
                    modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                    transformation = SurfaceTransformation(transformationSpec),
                ) {
                    Text(
                        text = stringResource(R.string.no_location),
                        textAlign = TextAlign.Center,
                    )
                }
            }
            item {
                Text(
                    text = stringResource(R.string.rationale),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                )
            }
            item {
                Button(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                    transformation = SurfaceTransformation(transformationSpec),
                ) {
                    Text(text = buttonText)
                }
            }
        }
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
private fun PermissionDeniedContentPreview() {
    WearAppTheme {
        PermissionDeniedContent(
            buttonText = stringResource(R.string.go_to_settings),
            onClick = {},
        )
    }
}
