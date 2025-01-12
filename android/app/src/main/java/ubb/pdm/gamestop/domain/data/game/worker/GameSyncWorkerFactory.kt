package ubb.pdm.gamestop.domain.data.game.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import ubb.pdm.gamestop.domain.data.game.GameRepository

class GameSyncWorkerFactory(
    private val gameRepository: GameRepository,
): WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker {
        return when (workerClassName) {
            GameSyncWorker::class.java.name ->
                GameSyncWorker(appContext, workerParameters, gameRepository)
            else -> throw IllegalArgumentException("Unknown worker class name: $workerClassName")
        }
    }
}