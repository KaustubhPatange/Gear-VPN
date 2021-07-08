package com.kpstv.composetest.extensions.utils

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import com.kpstv.composetest.services.VpnWorker

// A class to lazily initialize crucial dependencies that increases
// startup time eg: Work-manager
object Initializer {
  fun initialize(scope: LifecycleCoroutineScope, context: Context) {
    scope.launchWhenStarted {
      VpnWorker.schedule(context)
    }
  }
}