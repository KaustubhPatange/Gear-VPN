package com.kpstv.vpn.services

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.kpstv.vpn.data.api.IpApi
import com.kpstv.vpn.data.api.VpnApi
import com.kpstv.vpn.data.db.localized.VpnDao
import com.kpstv.vpn.di.service.worker.DaggerWorkerFactory
import com.kpstv.vpn.extensions.utils.NetworkUtils
import com.kpstv.vpn.extensions.utils.Notifications
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.supervisorScope
import okhttp3.CacheControl
import java.util.concurrent.TimeUnit

class VpnWorker @AssistedInject constructor(
  @Assisted private val appContext: Context,
  @Assisted workerParams: WorkerParameters,
  private val ipApi: IpApi,
  private val vpnApi: VpnApi,
  private val dao: VpnDao,
  networkUtils: NetworkUtils,
) : CoroutineWorker(appContext, workerParams) {

  override suspend fun doWork(): Result = supervisorScope scope@{
    setForeground(Notifications.createVpnRefreshNotification(appContext, this@VpnWorker))

    try {
      vpnApi.getVpnConfigs(cacheControl = CacheControl.FORCE_NETWORK, limit = 50)
    } catch(_: Exception) {
      createFailureResult()
    }

    return@scope Result.success()
  }

  private fun createFailureResult(): Result {
    Notifications.createVpnRefreshFailedNotification(appContext)
    return Result.failure()
  }

  @AssistedFactory
  interface Factory : DaggerWorkerFactory<VpnWorker>

  companion object {
    private const val ID = "com.kpstv.vpn:VPN-Worker"

    fun schedule(context: Context) {
      val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

      val request = PeriodicWorkRequestBuilder<VpnWorker>(6, TimeUnit.HOURS, 5, TimeUnit.MINUTES)
        .setConstraints(constraints)
        .build()

      WorkManager.getInstance(context)
        .enqueueUniquePeriodicWork(ID, ExistingPeriodicWorkPolicy.REPLACE, request)
    }

  }
}