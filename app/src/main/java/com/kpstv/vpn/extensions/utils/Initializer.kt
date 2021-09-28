package com.kpstv.vpn.extensions.utils

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import com.kpstv.vpn.BuildConfig
import com.kpstv.vpn.services.VpnWorker
import com.kpstv.vpn.ui.helpers.Settings

// A class to lazily initialize crucial dependencies that increases
// startup time eg: Work-manager
object Initializer {
  fun initialize(scope: LifecycleCoroutineScope, context: Context) {
    scope.launchWhenStarted {
      if (BuildConfig.DEBUG || Settings.isFirstLaunchAndSet()) {
        VpnWorker.schedule(context)
      }
    }
  }
}