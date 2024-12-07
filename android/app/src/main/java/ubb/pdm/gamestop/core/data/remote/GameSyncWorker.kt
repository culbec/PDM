package ubb.pdm.gamestop.core.data.remote

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class GameSyncWorker(
    val context: Context,
    val params: WorkerParameters
): CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        TODO("Not yet implemented")
    }
}