package ubb.pdm.gamestop.core.network

import kotlinx.coroutines.flow.Flow

interface ConnectivityObserver {
    val isConnected: Flow<Boolean>
}