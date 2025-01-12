package ubb.pdm.gamestop.core.util

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalPermissionsApi
@Composable
fun RequirePermissions(
    permissions: List<String>,
    modifier: Modifier,
    content: @Composable () -> Unit = {}
) {
    val permissionsState = rememberMultiplePermissionsState(permissions = permissions)

    if (permissionsState.allPermissionsGranted) {
        content()
    } else {
        Column(
            modifier = modifier
                .background(color = Color.White)
                .padding(24.dp),
        ) {

            Text(
                getRevokedPermissions(
                    permissionsState.revokedPermissions,
                    permissionsState.shouldShowRationale
                )
            )
            Spacer(
                modifier = Modifier.height(8.dp)
            )
            Row {
                Button(
                    onClick = {
                        Log.d("RequirePermissions", "Requesting permissions")
                        permissionsState.launchMultiplePermissionRequest()
                        Log.d(
                            "RequirePermissions",
                            "Permissions granted: ${permissionsState.allPermissionsGranted}"
                        )
                    }
                ) {
                    Text("Request permissions")
                }
                Button(
                    onClick = {
                        Log.d("RequirePermissions", "Permissions denied")
                    }
                ) {
                    Text("Deny")
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
private fun getRevokedPermissions(
    permissionStates: List<PermissionState>,
    shouldShowRationale: Boolean
): String {
    val revokedPermissionsSize = permissionStates.size
    if (revokedPermissionsSize == 0) {
        return "All permissions granted"
    }

    val message =
        "The following permissions: " + permissionStates.joinToString(", ") { it.permission } + " were denied.\n"

    return if (shouldShowRationale) {
        message + "Please grant all permissions to continue."
    } else {
        message + "The app cannot function without these permissions."
    }
}