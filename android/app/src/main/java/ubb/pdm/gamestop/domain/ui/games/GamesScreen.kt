package ubb.pdm.gamestop.domain.ui.games

import android.util.Log
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ubb.pdm.gamestop.R
import ubb.pdm.gamestop.domain.data.photo.saveImageToFile
import ubb.pdm.gamestop.domain.ui.photos.PhotosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesScreen(
    onItemClick: OnItemFn,
    onAddItem: () -> Unit,
    onLogout: () -> Unit,
) {
    val tag by rememberSaveable { mutableStateOf("GamesScreen") }
    val context = LocalContext.current

    val gamesViewModel = viewModel<GamesViewModel>(factory = GamesViewModel.Factory)
    val gamesState by gamesViewModel.gamesStream.collectAsStateWithLifecycle(
        initialValue = listOf()
    )

    val photosViewModel = viewModel<PhotosViewModel>(factory = PhotosViewModel.Factory)
    val photosState by photosViewModel.photosStream.collectAsStateWithLifecycle(
        initialValue = listOf()
    )

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
                title = { Text(text = stringResource(R.string.games)) },
                actions = {
                    Button(
                        onClick = { onLogout() },
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp)
                    ) { Text(text = "Logout") }
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
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (gamesViewModel.gamesState.value.isLoading || photosViewModel.photosState.value.isLoading) {
                CircularProgressIndicator()
                return@Scaffold
            }

            GameList(
                gameList = gamesState,
                photoList = photosState,
                onItemClick = onItemClick,
                modifier = Modifier.padding(it)
            )
        }
    }
}

@Preview
@Composable
fun GamesScreenPreview() {
    GamesScreen({}, {}, {})
}