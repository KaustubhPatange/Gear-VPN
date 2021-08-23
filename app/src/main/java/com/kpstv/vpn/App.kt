package com.kpstv.vpn

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.kpstv.vpn.logging.Logger
import com.kpstv.vpn.extensions.utils.Notifications
import com.kpstv.vpn.ui.helpers.Settings
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {

  @Inject lateinit var workerFactory: HiltWorkerFactory

  override fun getWorkManagerConfiguration() =
    Configuration.Builder()
      .setWorkerFactory(workerFactory)
      .build()

  override fun onCreate() {
    super.onCreate()
    Notifications.init(this)

    Logger.init(BuildConfig.DEBUG)
    Settings.init(this)

    if (BuildConfig.DEBUG) {
      // disable crashlytics & analytics for debug builds
      Logger.disable(this)
    }

  }
}