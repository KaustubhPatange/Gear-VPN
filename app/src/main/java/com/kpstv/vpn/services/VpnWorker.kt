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

    /**
     * We don't need this worker since everything is moved to API now which is much
     * reliable & fast, but we'll still keep this periodic worker in place as it
     * might help for any future tasks.
     */
    return@scope Result.success()

//    Logger.d("Fetching from Worker")
//
//    // fetch IP for logging
//    try {
//      val ipData = ipApi.fetch()
//      Logger.d("IP Info: ${ipData.country}, ${ipData.city}, ${ipData.region}")
//    } catch (_: Exception) {
//    }
//
//    val vpnGateListAsync = async { vpnGateParser.parse() }
//    val vpnBookListAsync = async { vpnBookParser.parse() }
//    val duoServerListAsync = async { vpnApi.getDuoServers() }
//
//    val vpnGateConfigList = try {
//      vpnGateListAsync.await()
//    } catch (e: Exception) {
//      Logger.w(e, "Failed to fetch VPN servers from vpngate.net for Worker")
//      emptyList()
//    }
//
//    val vpnBookList = try {
//      vpnBookListAsync.await()
//    } catch (e: Exception) {
//      Logger.w(e, "Failed to fetch VPN servers from vpnbook.com for Worker")
//      emptyList()
//    }
//
//    val duoServerList = try {
//      duoServerListAsync.await()
//    } catch (e: Exception) {
//      Logger.w(e, "Failed to fetch VPN servers from gear-vpn-api/duoserver for Worker")
//      emptyList()
//    }
//
//    val final = VpnRepository.merge(vpnGateConfigList, vpnBookList, duoServerList)
//
//    return@scope if (final.isNotEmpty()) {
//      dao.insertAll(final)
//      Result.success()
//    } else {
//      createFailureResult()
//    }
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