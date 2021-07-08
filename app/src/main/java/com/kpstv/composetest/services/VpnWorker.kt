package com.kpstv.composetest.services

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.kpstv.composetest.data.db.localized.VpnDao
import com.kpstv.composetest.data.helpers.OpenApiParser
import com.kpstv.composetest.extensions.utils.NetworkUtils
import com.kpstv.composetest.extensions.utils.Notifications
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

// A worker to refresh VPN configurations every 7 hours.
@HiltWorker
class VpnWorker @AssistedInject constructor(
  @Assisted private val appContext: Context,
  @Assisted workerParams: WorkerParameters,
  private val dao: VpnDao,
  networkUtils: NetworkUtils,
): CoroutineWorker(appContext, workerParams) {

  private val parser = OpenApiParser(networkUtils)

  override suspend fun doWork(): Result {
    setForeground(Notifications.createRefreshNotification(appContext))

    val list = parser.parse()

    return if (list.isNotEmpty()) {
      dao.insertAll(list)
      Result.success()
    } else {
      Result.failure()
    }
  }


  companion object {
    private const val ID = "com.kpstv.vpn:VPN-Worker"

    fun schedule(context: Context) {
      val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

      val request = PeriodicWorkRequestBuilder<VpnWorker>(7, TimeUnit.HOURS, 5, TimeUnit.MINUTES)
        .setConstraints(constraints)
        .build()

      WorkManager.getInstance(context).enqueueUniquePeriodicWork(ID, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    fun stop(context: Context) {
      WorkManager.getInstance(context).cancelUniqueWork(ID)
    }
  }
}