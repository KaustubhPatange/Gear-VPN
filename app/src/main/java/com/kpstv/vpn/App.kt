package com.kpstv.vpn

import android.app.Application
import androidx.work.Configuration
import com.kpstv.vpn.di.app.AppComponent
import com.kpstv.vpn.di.app.DaggerAppComponent
import com.kpstv.vpn.di.service.worker.InjectDaggerWorkerFactory
import com.kpstv.vpn.logging.Logger
import com.kpstv.vpn.extensions.utils.Notifications
import com.kpstv.vpn.ui.helpers.Device
import com.kpstv.vpn.ui.helpers.Settings
import javax.inject.Inject

class App : Application(), Configuration.Provider {

  val appComponent: AppComponent by lazy {
    DaggerAppComponent.factory()
      .create(this)
  }

  @Inject lateinit var workerFactory: dagger.Lazy<InjectDaggerWorkerFactory>

  override fun getWorkManagerConfiguration() =
    Configuration.Builder()
      .setWorkerFactory(workerFactory.get())
      .build()

  override fun onCreate() {
    appComponent.inject(this)
    super.onCreate()
    Device.init(this)

    Logger.init(BuildConfig.DEBUG)
    Settings.init(this)

    if (BuildConfig.DEBUG) {
      // disable crashlytics & analytics for debug builds
      Logger.disable(this)
    }

  }
}