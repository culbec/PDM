package ubb.pdm.gamestop.domain.ui.game

import android.app.Application
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import ubb.pdm.gamestop.R
import ubb.pdm.gamestop.domain.ui.location.MapComposable
import ubb.pdm.gamestop.domain.ui.location.MapViewModel
import ubb.pdm.gamestop.domain.ui.location.OnMapClick
import ubb.pdm.gamestop.domain.ui.location.OnMarkerClick
import ubb.pdm.gamestop.core.ui.UserPreferencesViewModel
import ubb.pdm.gamestop.core.util.Result
import ubb.pdm.gamestop.core.util.formatTo
import ubb.pdm.gamestop.core.util.toDate
import ubb.pdm.gamestop.domain.data.game.GameCategory

// TODO: integrate map and assign custom map method to modify the location of the game and retrieve
// TODO: place the marker on the game's location

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    gameId: String?,
    onClose: () -> Unit,
    onMapClick: OnMapClick?,
    onMarkerClick: OnMarkerClick?
) {
    val userPreferencesViewModel =
        viewModel<UserPreferencesViewModel>(factory = UserPreferencesViewModel.Factory)

    val mapViewModel =
        viewModel<MapViewModel>(factory = MapViewModel.Factory(LocalContext.current.applicationContext as Application))

    val gameViewModel = viewModel<GameViewModel>(factory = GameViewModel.Factory(gameId))
    val gameState: GameState by gameViewModel.gameState

    var game by remember { mutableStateOf(gameState.game) }
    var gameInitialized by rememberSaveable { mutableStateOf(gameId == null) }

    var showDatePickerDialog by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    var sliderPosition by remember { mutableIntStateOf(game.rating) }

    var hasErrors by remember { mutableStateOf(false) }
    var errors by remember { mutableStateOf("") }

    suspend fun handleSaveOrUpdateGame() {
        errors = ""

        if (game.title.isEmpty()) {
            errors += "Title is required"
        }

        try {
            val price = game.rentalPrice
            if (price < 0f || price > 100f) {
                errors += "Rental price must be between 0 and 100"
            }
        } catch (_: Exception) {
            errors += "Invalid rental price"
        }

        if (errors.isNotEmpty()) {
            hasErrors = true
            return
        }

        val username = userPreferencesViewModel.get("username")
        game = game.copy(username = username)
        gameViewModel.saveOrUpdateGame(game)
    }

    fun handleDelete() {
        if (gameId != null) {
            gameViewModel.deleteGame(game)
        }
    }

    // what to do on submit?
    LaunchedEffect(gameState.submitResult) {
        if (gameState.submitResult is Result.Success) {
            Log.d("GameScreen", "Closing screen")
            delay(2000)
            onClose()
        } else if (gameState.submitResult is Result.Error) {
            // check for unauthorized
            val error = gameState.submitResult as Result.Error

            if (error.exception?.message?.contains("401") == true) {
                // unauthorized
                Log.d("GameScreen", "Unauthorized")
                onClose()
            }

            errors = error.exception?.message ?: "Unknown error"
            hasErrors = true
        }
    }

    LaunchedEffect(gameState.loadResult) {
        if (gameInitialized) {
            return@LaunchedEffect
        }

        if (gameState.loadResult is Result.Error) {
            // check for unauthorized
            val error = gameState.loadResult as Result.Error

            if (error.exception?.message?.contains("401") == true) {
                // unauthorized
                Log.d("GameScreen", "Unauthorized")
                onClose()
            }

            errors = error.exception?.message ?: "Unknown error"
            hasErrors = true
        }

        if (gameState.loadResult !is Result.Loading) {
            game = gameState.game
            gameInitialized = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (gameState.game.id.isNotEmpty()) {
                        Text(text = stringResource(R.string.game_edit))
                    } else {
                        Text(text = stringResource(R.string.game_add))
                    }
                },
                actions = {
                    if (gameId != null) {
                        Button(
                            onClick = {
                                runBlocking(Dispatchers.IO) {
                                    handleDelete()
                                }
                            }, modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp)
                        ) {
                            Text("Delete")
                        }
                    }
                    
                    Button(
                        onClick = {
                            runBlocking(Dispatchers.IO) {
                                handleSaveOrUpdateGame()
                            }
                        },
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp)
                    ) {
                        if (gameId != null) {
                            Text("Update")
                        } else {
                            Text("Save")
                        }
                    }
                }
            )
        }
    ) {
        // errors dialog
        if (hasErrors) {
            BasicAlertDialog(
                onDismissRequest = {
                    hasErrors = false
                },
                modifier = Modifier
                    .background(Color.White)
                    .padding(16.dp),
            ) {
                Column {
                    Text(
                        text = "Error",
                        modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
                    )
                    Text(
                        text = errors,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                        color = Color.Red
                    )
                    Button(
                        onClick = {
                            hasErrors = false
                        },
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(16.dp),
                    ) {
                        Text(text = "OK")
                    }
                }
            }
        }

        // success dialog
        if (gameState.submitResult is Result.Success) {
            BasicAlertDialog(
                onDismissRequest = {
                    onClose()
                },
                modifier = Modifier
                    .background(Color.White)
                    .padding(16.dp),
            ) {
                Column {
                    Text(
                        text = "Success",
                        modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
                    )
                    Text(
                        text = "Game saved successfully",
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                        color = colorResource(R.color.dark_navy)
                    )
                    Button(
                        onClick = {
                            onClose()
                        },
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(16.dp),
                    ) {
                        Text(text = "OK")
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            // game is loading
            if (gameState.loadResult is Result.Loading) {
                CircularProgressIndicator()
                return@Scaffold
            }

            // operation on game is loading
            if (gameState.submitResult is Result.Loading) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LinearProgressIndicator()
                }
            }

            // error loading game
            if (gameState.loadResult is Result.Error) {
                errors =
                    (gameState.loadResult as Result.Error).exception?.message ?: "Unknown error"
                hasErrors = true
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MapComposable(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.5f)
                        .padding(16.dp),
                    mapViewModel = mapViewModel,
                    onMapClick = onMapClick,
                    onMarkerClick = onMarkerClick,
                    game = game
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .border(1.dp, Color.Black)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextField(
                        label = { Text(text = "Title") },
                        value = game.title,
                        placeholder = { Text(text = game.title) },
                        onValueChange = { game = game.copy(title = it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(9.dp)
                    )
                    TextField(
                        label = { Text(text = "Rental price") },
                        value = game.rentalPrice.toString(),
                        placeholder = { Text(text = game.rentalPrice.toString()) },
                        onValueChange = {
                            val price = it.toFloatOrNull()

                            if (price != null) {
                                game = game.copy(rentalPrice = price)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(9.dp),
                        isError = game.rentalPrice < 0f || game.rentalPrice > 100f

                    )

                    Button(
                        onClick = { showDatePickerDialog = true },
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Select Release Date")
                    }

                    if (showDatePickerDialog) {
                        DatePickerDialog(
                            onDismissRequest = {
                                showDatePickerDialog = false
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        game =
                                            game.copy(
                                                releaseDate = datePickerState.selectedDateMillis?.toDate()
                                                    ?.formatTo() ?: ""
                                            )

                                        showDatePickerDialog = false
                                    }
                                ) {
                                    Text("OK")
                                }
                            },
                        ) {
                            DatePicker(
                                state = datePickerState,
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row {
                            Icon(Icons.Rounded.Star, contentDescription = null)
                            Text(text = "Rating: $sliderPosition")
                        }
                        Slider(
                            value = sliderPosition.toFloat(),
                            onValueChange = {
                                sliderPosition = it.toInt()
                                game = game.copy(rating = it.toInt())
                            },
                            valueRange = 1f..10f,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    GameCategoryDropdown(
                        selectedCategory = GameCategory.valueOf(game.category),
                        onCategorySelected = {
                            game = game.copy(category = it.value)
                        })
                }

            }
        }
    }
}

//@Preview
//@Composable
//fun GameScreenPreview() {
//    GameScreen(null,) { }
//}