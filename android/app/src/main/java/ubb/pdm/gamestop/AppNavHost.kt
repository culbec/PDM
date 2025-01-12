package ubb.pdm.gamestop

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import ubb.pdm.gamestop.auth.ui.AuthViewModel
import ubb.pdm.gamestop.auth.ui.LoginScreen
import ubb.pdm.gamestop.core.data.UserPreferences
import ubb.pdm.gamestop.core.ui.UserPreferencesViewModel
import ubb.pdm.gamestop.core.util.SessionManager
import ubb.pdm.gamestop.core.ws.WsViewModel
import ubb.pdm.gamestop.domain.data.game.Game
import ubb.pdm.gamestop.domain.data.game.Location
import ubb.pdm.gamestop.domain.data.game.worker.GameSyncViewModel
import ubb.pdm.gamestop.domain.ui.game.GameScreen
import ubb.pdm.gamestop.domain.ui.games.GamesScreen
import ubb.pdm.gamestop.domain.ui.location.LocationScreen

enum class AppRoutes(val value: String) {
    GAMES("games"),
    LOGIN("login"),
    LOCATION("location")
}

@Composable
fun AppNavHost(
    startingRoute: String = AppRoutes.LOGIN.value,
) {
    val navController = rememberNavController()
    val onCloseGame = {
        Log.d("AppNavHost", "navigate back to list")
        navController.popBackStack()
    }

    val userPreferencesViewModel =
        viewModel<UserPreferencesViewModel>(factory = UserPreferencesViewModel.Factory)
    val userPreferencesState by userPreferencesViewModel.userPreferencesState.collectAsStateWithLifecycle(
        initialValue = UserPreferences()
    )

    val authViewModel = viewModel<AuthViewModel>(factory = AuthViewModel.Factory)
    val gameSyncViewModel = viewModel<GameSyncViewModel>(factory = GameSyncViewModel.Factory)
    val wsViewModel: WsViewModel = viewModel<WsViewModel>(factory = WsViewModel.Factory)
    
    LaunchedEffect(key1 = userPreferencesState.token) {
        if (userPreferencesState.token.isNotEmpty()) {
            Log.d("AppNavHost", "[LAUNCHED EFFECT] start worker")
            gameSyncViewModel.startWorker()

            wsViewModel.setCredentials(userPreferencesState.username, userPreferencesState.token)
            
            Log.d("AppNavHost", "[LAUNCHED EFFECT] navigate to games")
            navController.navigate(AppRoutes.GAMES.value)
        }
    }

    LaunchedEffect(Unit) {
        // Wait for user prefs to be loaded
        userPreferencesViewModel.userPreferencesState.collect { prefs ->
            if (prefs.token.isEmpty()) {
                Log.d("AppNavHost", "no token, cancelling worker")
                gameSyncViewModel.cancelWorker()

                Log.d("AppNavHost", "no token, navigate to login")
                navController.navigate(AppRoutes.LOGIN.value) {
                    popUpTo(0)
                }
            }
        }
        
        // Go back to login on invalidated session
        SessionManager.sessionInvalidated.collect {
            navController.navigate(AppRoutes.LOGIN.value) {
                popUpTo(0)
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startingRoute
    ) {
        composable(route = AppRoutes.LOGIN.value) {
            LoginScreen(
                onClose = {
                    Log.d("AppNavHost", "navigate to games")
                    navController.navigate(AppRoutes.GAMES.value)
                }
            )
        }

        composable(route = AppRoutes.GAMES.value) {
            GamesScreen(
                onItemClick = { gameId ->
                    Log.d("AppNavHost", "navigate to item $gameId")
                    navController.navigate("${AppRoutes.GAMES.value}/$gameId")
                },
                onAddItem = {
                    Log.d("AppNavHost", "navigate to new game")
                    navController.navigate("${AppRoutes.GAMES.value}/new")
                },
                onLogout = {
                    Log.d("AppNavHost", "logout")

                    authViewModel.logout()
                    navController.navigate(AppRoutes.LOGIN.value) {
                        popUpTo(0)
                    }
                },
                onLocation = {
                    Log.d("AppNavHost", "navigate to location")
                    navController.navigate(AppRoutes.LOCATION.value)
                }
            )
        }

        composable(
            route = "${AppRoutes.GAMES.value}/new"
        ) {
            GameScreen(
                gameId = null,
                onClose = { onCloseGame() },
                onMapClick = { latLng: LatLng, game: Game? ->
                    if (game != null) {
                        Log.d("AppNavHost", "updating the game's position to $latLng")
                        game.location = Location(latLng.latitude, latLng.longitude)
                    }
                },
                onMarkerClick = null
            )
        }

        composable(
            route = "${AppRoutes.GAMES.value}/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            GameScreen(
                gameId = it.arguments?.getString("id"),
                onClose = { onCloseGame() },
                onMapClick = { latLng: LatLng, game: Game? ->
                    if (game != null) {
                        Log.d("AppNavHost", "updating the game's position to $latLng")
                        game.location = Location(latLng.latitude, latLng.longitude)
                    }
                },
                onMarkerClick = null
            )
        }

        composable(route = AppRoutes.LOCATION.value) {
            LocationScreen(
                onMapClick = null,
                onMarkerClick = {marker: Marker, game: Game? ->
                    return@LocationScreen if (game != null) {
                        Log.d("AppNavHost", "marker clicked: ${marker.position} | ${game.id}")
                        navController.navigate("${AppRoutes.GAMES.value}/${game.id}")
                        true
                    } else {
                        Log.d("AppNavHost", "marker clicked: ${marker.position} | no game")
                        false
                    }
                }
            )
        }
    }
}