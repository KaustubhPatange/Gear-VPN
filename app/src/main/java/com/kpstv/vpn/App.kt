package com.kpstv.vpn

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kpstv.vpn.extensions.utils.Logger
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

    if (BuildConfig.DEBUG) {
      // disable crashlytics & analytics for debug builds
      FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false)
      FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(false)
    }

    Logger.init()
    Settings.init(this)
  }
}