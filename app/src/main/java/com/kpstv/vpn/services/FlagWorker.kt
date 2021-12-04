package com.kpstv.vpn.services

import android.content.Context
import androidx.work.*
import com.kpstv.vpn.data.api.FlagApi
import com.kpstv.vpn.data.db.localized.FlagDao
import com.kpstv.vpn.di.service.worker.DaggerWorkerFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class FlagWorker @AssistedInject constructor(
  private val flagApi: FlagApi,
  private val flagDao: FlagDao,
  @Assisted private val appContext: Context,
  @Assisted workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

  override suspend fun doWork(): Result {
    val flags = flagApi.fetch()
    if (flags.isEmpty()) return Result.failure()

    flagDao.insert(flags)

    return Result.success()
  }

  @AssistedFactory
  interface Factory : DaggerWorkerFactory<FlagWorker>

  companion object {
    private const val ID = "com.kpstv.vpn:Flag-Worker"

    fun schedule(context: Context) {
      val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

      val request = OneTimeWorkRequestBuilder<FlagWorker>()
        .setConstraints(constraints)
        .build()

      WorkManager.getInstance(context)
        .enqueueUniqueWork(ID, ExistingWorkPolicy.REPLACE, request)
    }
  }
}