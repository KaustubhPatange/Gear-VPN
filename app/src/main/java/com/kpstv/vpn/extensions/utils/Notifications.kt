package com.kpstv.vpn.extensions.utils

import android.app.Notification
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
        NotificationChannel(REFRESH_CHANNEL, getString(R.string.channel_refresh), NotificationManager.IMPORTANCE_LOW)
      )

      notificationManager.createNotificationChannel(
        NotificationChannel(UPDATE_CHANNEL, getString(R.string.channel_update), NotificationManager.IMPORTANCE_LOW)
      )
    }
  }

  fun createRefreshNotification(context: Context): ForegroundInfo = with(context) {
    val cancelIntent = AppBroadcast.createPendingIntent(this, AppBroadcast.STOP_REFRESHING)

    val builder = NotificationCompat.Builder(this, REFRESH_CHANNEL)
      .setOngoing(true)
      .setContentTitle(getString(R.string.vpn_refresh))
      .setSmallIcon(R.drawable.ic_logo)
      .setProgress(100, 0, true)
      .setCategory(Notification.CATEGORY_SERVICE)
      .setPriority(NotificationCompat.PRIORITY_LOW)
      .addAction(R.drawable.ic_baseline_cancel_24, getString(android.R.string.cancel), cancelIntent)

    ForegroundInfo(NOTIFICATION_REFRESH, builder.build())
  }

  private fun cancel(context: Context, id: Int) {
    NotificationManagerCompat.from(context).cancel(id)
  }

  fun createDownloadingNotification(context: Context, progress: Int = -1): Unit = with(context) {
    val builder = NotificationCompat.Builder(this, UPDATE_CHANNEL).apply {
      setContentTitle(getString(R.string.vpn_update_download))
      setSmallIcon(android.R.drawable.stat_sys_download)
      if (progress == -1) setProgress(100, 0, true) else
        setProgress(100, progress, false)
    }

    NotificationManagerCompat.from(this).notify(NOTIFICATION_UPDATE, builder.build())
  }

  fun cancelDownloadingNotification(context: Context) {
    cancel(context, NOTIFICATION_UPDATE)
  }

  private const val REFRESH_CHANNEL = "refresh_channel"
  private const val UPDATE_CHANNEL = "refresh_update"

  private const val NOTIFICATION_REFRESH = 132
  private const val NOTIFICATION_UPDATE = 133
}