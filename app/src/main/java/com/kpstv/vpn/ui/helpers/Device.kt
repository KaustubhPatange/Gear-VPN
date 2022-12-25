package com.kpstv.vpn.ui.helpers

import android.content.Context
import android.provider.Settings

object Device {
  private var deviceIdMutable: String? = null
  val deviceId get() = deviceIdMutable!!

  fun init(appContext: Context) {
    deviceIdMutable = Settings.Secure.getString(
      appContext.contentResolver,
      Settings.Secure.ANDROID_ID
    )
  }
}