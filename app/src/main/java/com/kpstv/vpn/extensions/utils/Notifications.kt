package com.kpstv.vpn.extensions.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import androidx.work.ForegroundInfo
import com.kpstv.vpn.R
import com.kpstv.vpn.recievers.AppBroadcast

object Notifications {
  fun init(context: Context) = with(context) {
    if (Build.VERSION.SDK_INT >= 28) {
      val notificationManager = getSystemService<NotificationManager>()!!
      notificationManager.createNotificationChannel(
        NotificationChannel(
          REFRESH_CHANNEL,
          getString(R.string.channel_refresh),
          NotificationManager.IMPORTANCE_LOW
        )
      )
    }
  }

  fun createRefreshNotification(context: Context): ForegroundInfo = with(context) {
    val cancelIntent = AppBroadcast.createPendingIntent(this, AppBroadcast.STOP_REFRESHING)

    val builder = NotificationCompat.Builder(this, REFRESH_CHANNEL)
      .setContentTitle(getString(R.string.vpn_refresh))
      .setSmallIcon(R.drawable.ic_logo)
      .setProgress(100, 0, true)
      .addAction(R.drawable.ic_baseline_cancel_24, getString(android.R.string.cancel), cancelIntent)

    ForegroundInfo(NOTIFICATION_REFRESH, builder.build())
  }

  private fun cancel(context: Context, id: Int) {
    NotificationManagerCompat.from(context).cancel(id)
  }

  private const val REFRESH_CHANNEL = "refresh_channel"

  private const val NOTIFICATION_REFRESH = 132
}