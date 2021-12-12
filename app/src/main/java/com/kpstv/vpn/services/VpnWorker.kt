package com.kpstv.vpn.services

import android.content.Context
import androidx.work.*
import com.kpstv.vpn.data.api.IpApi
import com.kpstv.vpn.data.db.localized.VpnDao
import com.kpstv.vpn.data.db.repository.VpnRepository
import com.kpstv.vpn.data.helpers.VpnGateParser
import com.kpstv.vpn.data.helpers.VpnBookParser
import com.kpstv.vpn.di.service.worker.DaggerWorkerFactory
import com.kpstv.vpn.extensions.utils.NetworkUtils
import com.kpstv.vpn.extensions.utils.Notifications
import com.kpstv.vpn.logging.Logger
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

// A worker to refresh VPN configurations every 7 hours.
class VpnWorker @AssistedInject constructor(
  @Assisted private val appContext: Context,
  @Assisted workerParams: WorkerParameters,
  private val ipApi: IpApi,
  private val dao: VpnDao,
  networkUtils: NetworkUtils,
) : CoroutineWorker(appContext, workerParams) {

  private val vpnGateParser = VpnGateParser(networkUtils)
  private val vpnBookParser = VpnBookParser(networkUtils)

  override suspend fun doWork(): Result = supervisorScope scope@{
    setForeground(Notifications.createVpnRefreshNotification(appContext))

    Logger.d("Fetching from Worker")

    // fetch IP for logging
    try {
      val ipData = ipApi.fetch()
      Logger.d("IP Info: ${ipData.country}, ${ipData.city}, ${ipData.region}")
    } catch (_ : Exception) { }

    val vpnGateListAsync = async(start = CoroutineStart.LAZY) { vpnGateParser.parse() }
    val vpnBookListAsync = async(start = CoroutineStart.LAZY) { vpnBookParser.parse() }

    val configList = try {
      awaitAll(vpnGateListAsync, vpnBookListAsync)
    } catch (e : Exception) {
      Logger.w(e, "Failed to fetch VPN servers from Worker")
      return@scope createFailureResult()
    }

    val final = VpnRepository.merge(configList[0], configList[1])

    return@scope if (final.isNotEmpty()) {
      dao.insertAll(final)
      Result.success()
    } else {
      createFailureResult()
    }
  }

  private fun createFailureResult() : Result {
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

    fun stop(context: Context) {
      WorkManager.getInstance(context).cancelUniqueWork(ID)
    }
  }
}