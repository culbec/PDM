package ubb.pdm.gamestop.domain.ui.location

import android.Manifest
import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import ubb.pdm.gamestop.core.util.RequirePermissions
import ubb.pdm.gamestop.domain.data.game.Game

typealias OnMapClick = (LatLng, Game?) -> Unit
typealias OnMarkerClick = (Marker, Game?) -> Boolean

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapComposable(
    modifier: Modifier,
    mapViewModel: MapViewModel,
    onMapClick: OnMapClick?,
    onMarkerClick: OnMarkerClick?,
    game: Game? = null
) {
    val markers by mapViewModel.markers.collectAsStateWithLifecycle(initialValue = emptyList())

    val locationViewModel = viewModel<LocationViewModel>(
        factory = LocationViewModel.Factory(
            LocalContext.current.applicationContext as Application
        )
    )
    val location = locationViewModel.uiState

    var isMapLoaded by rememberSaveable { mutableStateOf(false) }

    RequirePermissions(
        permissions = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ),
        modifier = modifier
    ) {
        Column(
            modifier = modifier
                .background(color = Color.White)
                .border(1.dp, Color.Black)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (location == null) {
                LinearProgressIndicator(
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                val markerState = rememberMarkerState(
                    position = if (game != null) {
                        LatLng(game.location.latitude, game.location.longitude)
                    } else {
                        LatLng(location.latitude, location.longitude)
                    }
                )

                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(markerState.position, 12f)
                }

                LaunchedEffect(game?.location) {
                    game?.location?.let {
                        markerState.position = LatLng(it.latitude, it.longitude)
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        onMapClick = { latLng ->
                            markerState.position = latLng
                            onMapClick?.invoke(latLng, game)
                        },
                        onMapLoaded = {
                            isMapLoaded = true
                        }
                    ) {
                        if (isMapLoaded) {
                            Marker(
                                state = markerState,
                                title = game?.title ?: "Location",
                                snippet = "Latitude: ${markerState.position.latitude}, Longitude: ${markerState.position.longitude}",
                            )

                            markers.forEach { gameMarker ->
                                if (gameMarker.id != game?.id) {
                                    val gameMarkerState = rememberMarkerState(
                                        position = LatLng(
                                            gameMarker.location.latitude,
                                            gameMarker.location.longitude
                                        )
                                    )
                                    Marker(
                                        state = gameMarkerState,
                                        title = gameMarker.title,
                                        snippet = "Latitude: ${gameMarkerState.position.latitude}, Longitude: ${gameMarkerState.position.longitude}",
                                        onClick = { marker ->
                                            onMarkerClick?.invoke(marker, gameMarker) == true
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (!isMapLoaded) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}