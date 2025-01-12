package ubb.pdm.gamestop.domain.ui.location

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ubb.pdm.gamestop.R
import ubb.pdm.gamestop.domain.ui.games.GamesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationScreen(
    onMapClick: OnMapClick?,
    onMarkerClick: OnMarkerClick?,
) {
    val gamesViewModel = viewModel<GamesViewModel>(factory = GamesViewModel.Factory)
    val gamesState by gamesViewModel.gamesStream.collectAsStateWithLifecycle(
        initialValue = listOf()
    )

    val mapViewModel =
        viewModel<MapViewModel>(factory = MapViewModel.Factory(LocalContext.current.applicationContext as Application))

    // update markers on game list change
    LaunchedEffect(gamesState) {
        mapViewModel.updateMarkers(gamesState)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.location))
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, Color.Black)
                .background(Color.White)
                .padding(it),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MapComposable(
                modifier = Modifier
                    .fillMaxWidth(),
                mapViewModel = mapViewModel,
                onMapClick = onMapClick,
                onMarkerClick = onMarkerClick
            )

        }
    }
}