package com.kpstv.vpn.recievers

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kpstv.vpn.extensions.getRandomInt
import com.kpstv.vpn.extensions.utils.PendingIntentHelper
import com.kpstv.vpn.services.VpnWorker

class AppBroadcast : BroadcastReceiver() {
  companion object {
    const val STOP_REFRESHING = "com.kpstv.vpn:stop-refresh"

    fun createIntent(context: Context, action: String): Intent {
      return Intent(context, AppBroadcast::class.java).apply {
        setAction(action)
      }
    }

    fun createPendingIntent(context: Context, action: String, intent: Intent = createIntent(context, action)): PendingIntent {
      return PendingIntent.getBroadcast(context, getRandomInt(), intent, PendingIntentHelper.getSafeFlags())
    }
  }

  override fun onReceive(context: Context, intent: Intent) {
    when(intent.action) {
      STOP_REFRESHING -> VpnWorker.stop(context)
    }
  }
}