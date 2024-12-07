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
import ubb.pdm.gamestop.auth.ui.AuthViewModel
import ubb.pdm.gamestop.auth.ui.LoginScreen
import ubb.pdm.gamestop.core.SessionManager
import ubb.pdm.gamestop.core.data.UserPreferences
import ubb.pdm.gamestop.core.ui.UserPreferencesViewModel
import ubb.pdm.gamestop.domain.ui.game.GameScreen
import ubb.pdm.gamestop.domain.ui.games.GamesScreen

enum class AppRoutes(val value: String) {
    GAMES("games"),
    LOGIN("login")
}

@Composable
fun AppNavHost(startingRoute: String = AppRoutes.LOGIN.value) {
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

    LaunchedEffect(key1 = userPreferencesState.token) {
        if (userPreferencesState.token.isEmpty()) {
            Log.d("AppNavHost", "[LAUNCHED EFFECT] navigate to login")
            navController.navigate(AppRoutes.LOGIN.value) {
                popUpTo(0)
            }
        } else {
            Log.d("AppNavHost", "[LAUNCHED EFFECT] navigate to games")
            navController.navigate(AppRoutes.GAMES.value)
        }
    }

    LaunchedEffect(Unit) {
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
                }
            )
        }

        composable(
            route = "${AppRoutes.GAMES.value}/new"
        ) {
            GameScreen(
                gameId = null,
                onClose = { onCloseGame() }
            )
        }

        composable(
            route = "${AppRoutes.GAMES.value}/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            GameScreen(
                gameId = it.arguments?.getString("id"),
                onClose = { onCloseGame() }
            )
        }
    }
}