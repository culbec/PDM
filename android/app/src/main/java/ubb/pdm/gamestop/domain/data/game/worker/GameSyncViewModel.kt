package ubb.pdm.gamestop.domain.data.game.worker

import java.util.concurrent.TimeUnit
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.work.WorkManager
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import kotlinx.coroutines.launch
import ubb.pdm.gamestop.App
import ubb.pdm.gamestop.core.util.TAG
import java.util.UUID

const val WORKER_NAME: String = "game_sync_worker"
const val WORKER_TAG: String = "game_sync_worker_tag"

data class GameSyncState(
    val isRunning: Boolean = false,
    val errors: String = ""
)

class GameSyncViewModel(
    private val workManager: WorkManager,
): ViewModel() {
    private var _gameSyncState = mutableStateOf(GameSyncState())
    val gameSyncState: MutableState<GameSyncState> = _gameSyncState
    
    private var workId: UUID? = null
    
    fun startWorker() {
        Log.d(TAG, "startWorker")
        
        viewModelScope.launch {
            // Check if there is a worker with the same name already running
            val existingWorkInfos = workManager.getWorkInfosForUniqueWork(WORKER_NAME)
            if (existingWorkInfos.get().isNotEmpty()) {
                val workInfo = existingWorkInfos.get().first()
                Log.d(TAG, "existing worker state: ${workInfo.state}")
                
                if (workInfo.state == WorkInfo.State.ENQUEUED || workInfo.state == WorkInfo.State.RUNNING) {
                    Log.d(TAG, "worker already running")
                    return@launch
                }
            }
            
            // Only sync when there is an internet connection
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            // 1 minute periodic work
            val myWork = PeriodicWorkRequestBuilder<GameSyncWorker>(1, TimeUnit.MINUTES)
                .addTag(WORKER_TAG)
                .setConstraints(constraints)
                .build()
            
            workId = myWork.id
            _gameSyncState.value = _gameSyncState.value.copy(isRunning = true)
            
            workManager.apply {
                enqueueUniquePeriodicWork(WORKER_NAME, ExistingPeriodicWorkPolicy.REPLACE, myWork)
                getWorkInfoByIdLiveData(workId!!).asFlow().collect {
                    Log.d(TAG, "$it")

                    if (it != null) {
                        _gameSyncState.value = _gameSyncState.value.copy(
                            isRunning = it.state.isFinished == true,
                            errors = it.outputData.getString("errors").toString()
                        )
                    }
                    
                    if (it?.state?.isFinished == true) {
                        _gameSyncState.value = _gameSyncState.value.copy(
                            isRunning = false
                        )
                    }
                }
            }
        }
    }
    
    fun cancelWorker() {
        Log.d(TAG, "cancelWorker")
        
        viewModelScope.launch {
            workManager.cancelUniqueWork(WORKER_NAME)
        }
    }
    
//    fun cancelAllWorkers() {
//        Log.d(TAG, "cancelAllWorkers")
//        
//        viewModelScope.launch {
//            workManager.cancelAllWork()
//        }
//    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as App)
                GameSyncViewModel(app.container.workManager)
            }
        }
    }
}