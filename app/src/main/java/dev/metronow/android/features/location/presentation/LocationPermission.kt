package dev.metronow.android.features.location.presentation

import android.Manifest
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dev.metronow.android.R

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermission(
    rationale: String,
    permissionNotAvailableContent: @Composable () -> Unit = {},
    content: @Composable () -> Unit = {},
) {
    val permissions: MutableList<String> = mutableListOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    val permissionState = rememberMultiplePermissionsState(permissions)

    if (permissionState.allPermissionsGranted) {
        content()
    } else {
        Scaffold { paddingValues ->
            Column (modifier = Modifier.padding(paddingValues)) {
                if (permissionState.shouldShowRationale) {
                    AlertDialog(
                        onDismissRequest = { },
                        title = { Text(text = stringResource(id = R.string.permission_request)) },
                        text = { Text(rationale) },
                        confirmButton = {
                            Button(onClick = { permissionState.launchMultiplePermissionRequest() }) {
                                Text(text = stringResource(id = R.string.ok))
                            }
                        }
                    )
                } else {
                    // Either user got a permission request for the first time or declined two times or more
                    LaunchedEffect(Unit) {
                        permissionState.launchMultiplePermissionRequest()
                    }
                    permissionNotAvailableContent()
                }
            }
        }
    }
}
