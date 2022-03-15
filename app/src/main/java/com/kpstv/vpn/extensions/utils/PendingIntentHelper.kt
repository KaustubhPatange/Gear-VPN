package com.kpstv.vpn.extensions.utils

import android.app.PendingIntent
import android.os.Build

object PendingIntentHelper {
  fun getSafeFlags(): Int = if (Build.VERSION.SDK_INT >= 30) PendingIntent.FLAG_IMMUTABLE else 0 or PendingIntent.FLAG_CANCEL_CURRENT
}
