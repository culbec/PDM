package ubb.pdm.gamestop.core

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object SessionManager {
    private val _sessionInvalidated = MutableSharedFlow<Unit>()
    val sessionInvalidated = _sessionInvalidated.asSharedFlow()

    suspend fun invalidateSession() {
        _sessionInvalidated.emit(Unit)
    }
}