package ubb.pdm.gamestop.domain.ui.games

import android.app.Application
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ubb.pdm.gamestop.R
import ubb.pdm.gamestop.domain.ui.location.MapViewModel
import ubb.pdm.gamestop.core.network.ui.MyNetworkStatus
import ubb.pdm.gamestop.core.notifications.MyNotifications
import ubb.pdm.gamestop.domain.ui.sensors.AccelerometerSensor
import ubb.pdm.gamestop.core.ws.ui.WsNotifications
import ubb.pdm.gamestop.domain.data.game.worker.GameSyncViewModel
import ubb.pdm.gamestop.domain.data.photo.saveImageToFile
import ubb.pdm.gamestop.domain.ui.photos.PhotosViewModel

const val tag = "GamesScreen"
const val CHANNEL_ID = "GameStopChannel"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesScreen(
    onItemClick: OnItemFn,
    onAddItem: () -> Unit,
    onLogout: () -> Unit,
    onLocation: () -> Unit,
) {
    val context = LocalContext.current

    val gamesViewModel = viewModel<GamesViewModel>(factory = GamesViewModel.Factory)
    val gamesState by gamesViewModel.gamesStream.collectAsStateWithLifecycle(
        initialValue = listOf()
    )

    val photosViewModel = viewModel<PhotosViewModel>(factory = PhotosViewModel.Factory)
    val photosState by photosViewModel.photosStream.collectAsStateWithLifecycle(
        initialValue = listOf()
    )

    val gameSyncViewModel = viewModel<GameSyncViewModel>(factory = GameSyncViewModel.Factory)
    val gameSyncState = gameSyncViewModel.gameSyncState

    // create notification channel on init
    LaunchedEffect(Unit) {
        MyNotifications.createNotificationChannel(CHANNEL_ID, context)
    }

    // show notification on state sync start/finish
    LaunchedEffect(gameSyncState) {
        if (gameSyncState.value.isRunning) {
            // game sync running, show notification

            MyNotifications.showSimpleNotification(
                context,
                CHANNEL_ID,
                1,
                "Game Sync",
                "Game sync started."
            )
        } else {
            // gameSync finished
            // check for errors or success
            Log.d(tag, "gameSyncState: ${gameSyncState.value}")

            if (gameSyncState.value.errors.isNotEmpty()) {
                MyNotifications.showSimpleNotification(
                    context,
                    CHANNEL_ID,
                    1,
                    "Game Sync",
                    "Game sync failed."
                )
            }
        }

        MyNotifications.showSimpleNotification(
            context,
            CHANNEL_ID,
            1,
            "Game Sync",
            "Game sync finished. Result OK!"
        )
    }
    
    // save photos in memory on load
    LaunchedEffect(photosState) {
        // collecting non-existing photos from the current source
        val nonWrittenPhotos = photosState.filter { photo -> photo.localFilePath == null }
        nonWrittenPhotos.forEach { photo ->
            photo.saveImageToFile(context)
        }
    }

    Log.d(tag, "recompose")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.games))
                },
                actions = {
                    Button(
                        onClick = { onLogout() },
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp)
                    ) { Text(text = "Logout") }
                    Button(
                        onClick = { onLocation() },
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp)
                    ) {Text(text = "Location") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    Log.d(tag, "add game")
                    onAddItem()
                }
            ) {
                Icon(Icons.Rounded.Add, "Add")
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MyNetworkStatus()
            WsNotifications()
            AccelerometerSensor()

            if (gamesViewModel.gamesState.value.isLoading || photosViewModel.photosState.value.isLoading) {
                CircularProgressIndicator()
                return@Scaffold
            }

            GameList(
                gameList = gamesState,
                photoList = photosState,
                onItemClick = onItemClick,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

//@Preview
//@Composable
//fun GamesScreenPreview() {
//    val onMapClick = { latLng: LatLng, gameId: String? ->
//        Log.d("AppNavHost", "map click: $latLng, $gameId")
//    }
//    GamesScreen({}, onMapClick = onMapClick, {}, {})
//}