package com.kpstv.vpn.services

import android.content.Context
import androidx.work.*
import com.kpstv.vpn.data.db.localized.VpnBookDao
import com.kpstv.vpn.data.models.AppSettingsConverter
import com.kpstv.vpn.di.service.worker.DaggerWorkerFactory
import com.kpstv.vpn.extensions.setExpeditedCompat
import com.kpstv.vpn.extensions.utils.NetworkUtils
import com.kpstv.vpn.extensions.utils.NetworkUtils.Companion.getBodyAndClose
import com.kpstv.vpn.extensions.utils.Notifications
import com.kpstv.vpn.logging.Logger
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

// Class to update password from vpnbook.com
class VpnBookWorker @AssistedInject constructor(
  @Assisted private val appContext: Context,
  @Assisted workerParams: WorkerParameters,
  private val networkUtils: NetworkUtils,
  private val dao: VpnBookDao
) : CoroutineWorker(appContext, workerParams) {

  override suspend fun getForegroundInfo(): ForegroundInfo {
    return Notifications.createVpnBookRefreshNotification(appContext, this)
  }

  override suspend fun doWork(): Result {
    Logger.d("Trying to update credentials for vpnbook.com")

    val appSettingResponse = networkUtils.simpleGetRequest(SettingsUrl).getOrNull()
    if (appSettingResponse?.isSuccessful == true) {
      val content = appSettingResponse.getBodyAndClose()
      AppSettingsConverter.fromStringToAppSettings(content)?.let { converter ->
        dao.safeUpdate(username = converter.vpnbook.username, password = converter.vpnbook.password)
      }
    }

    return Result.success()
  }

  @AssistedFactory
  interface Factory : DaggerWorkerFactory<VpnBookWorker>

  companion object {
    private const val SettingsUrl = "https://raw.githubusercontent.com/KaustubhPatange/Gear-VPN/master/settings.json"
    private const val ID = "com.kpstv.vpn:VPN-Book-Worker"

    fun schedule(context: Context) {
      val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

      val request = OneTimeWorkRequestBuilder<VpnBookWorker>()
        .setConstraints(constraints)
        .setExpeditedCompat(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
        .build()

      WorkManager.getInstance(context)
        .enqueueUniqueWork(ID, ExistingWorkPolicy.REPLACE, request)
    }
  }
}