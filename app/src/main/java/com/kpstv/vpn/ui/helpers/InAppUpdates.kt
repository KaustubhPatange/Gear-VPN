package com.kpstv.vpn.ui.helpers

import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.kpstv.vpn.extensions.utils.Notifications

class InAppUpdates(private val activity: ComponentActivity) {
  private val appUpdateManager = AppUpdateManagerFactory.create(activity)

  private val updatedListener = InstallStateUpdatedListener { state ->
    when(state.installStatus()) {
      InstallStatus.DOWNLOADING -> {
        val progress = ((state.bytesDownloaded() * 100) / state.totalBytesToDownload()).toInt()
        Notifications.createDownloadingNotification(activity, progress)
      }
      InstallStatus.DOWNLOADED -> {
        Notifications.cancelDownloadingNotification(activity)
        appUpdateManager.completeUpdate()
      }
      else -> Notifications.cancelDownloadingNotification(activity)
    }
  }

  init {
    appUpdateManager.registerListener(updatedListener)
    activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
      override fun onDestroy(owner: LifecycleOwner) {
        appUpdateManager.unregisterListener(updatedListener)
      }
    })
  }

  fun init() {
    val appUpdateInfoTask = appUpdateManager.appUpdateInfo
    appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
      android.util.Log.d("UpdateHelper", "Update Availability: ${appUpdateInfo.updateAvailability()}")
      if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
        && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
      ) {
        appUpdateManager.startUpdateFlowForResult(
          appUpdateInfo,
          activity,
          AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE)
            .setAllowAssetPackDeletion(true)
            .build(),
          111) // I don't care
      }
    }
  }
}