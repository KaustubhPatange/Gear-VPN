package com.kpstv.vpn.services

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.kpstv.vpn.data.db.localized.VpnDao
import com.kpstv.vpn.data.helpers.OpenApiParser
import com.kpstv.vpn.data.helpers.VpnBookParser
import com.kpstv.vpn.extensions.utils.NetworkUtils
import com.kpstv.vpn.extensions.utils.Notifications
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

// A worker to refresh VPN configurations every 7 hours.
@HiltWorker
class VpnWorker @AssistedInject constructor(
  @Assisted private val appContext: Context,
  @Assisted workerParams: WorkerParameters,
  private val dao: VpnDao,
  networkUtils: NetworkUtils,
) : CoroutineWorker(appContext, workerParams) {

  private val openApiParser = OpenApiParser(networkUtils)
  private val vpnBookParser = VpnBookParser(networkUtils)

  override suspend fun doWork(): Result {
    setForeground(Notifications.createRefreshNotification(appContext))

    val openList = openApiParser.parse()
    val vpnList = vpnBookParser.parse()

    val final = openList.union(vpnList).distinctBy { it.ip }.sortedBy { it.country }
      .sortedByDescending { it.premium }

    return if (final.isNotEmpty()) {
      dao.insertAll(final)
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

      WorkManager.getInstance(context)
        .enqueueUniquePeriodicWork(ID, ExistingPeriodicWorkPolicy.REPLACE, request)
    }

    fun stop(context: Context) {
      WorkManager.getInstance(context).cancelUniqueWork(ID)
    }
  }
}