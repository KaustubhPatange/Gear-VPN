package com.kpstv.vpn.extensions.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.getSystemService
import androidx.work.ForegroundInfo
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import com.kpstv.vpn.R
import com.kpstv.vpn.ui.activities.Main
import com.kpstv.vpn.ui.activities.Splash
import es.dmoral.toasty.Toasty

object Notifications {
  fun init(context: ComponentActivity) = with(context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      val launcher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
          if (!result) {
            Toasty.error(this, getString(R.string.err_notification_permission)).show()
          }
        }
      launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val notificationManager = getSystemService<NotificationManager>()!!
      notificationManager.createNotificationChannel(
        NotificationChannel(REFRESH_CHANNEL, getString(R.string.channel_refresh), NotificationManager.IMPORTANCE_LOW)
      )

      notificationManager.createNotificationChannel(
        NotificationChannel(UPDATE_CHANNEL, getString(R.string.channel_update), NotificationManager.IMPORTANCE_LOW)
      )

      notificationManager.createNotificationChannel(
        NotificationChannel(COMMON_ALERT_CHANNEL, getString(R.string.channel_common_alert), NotificationManager.IMPORTANCE_DEFAULT)
      )
    }
  }
  fun createVpnRefreshNotification(context: Context, worker: ListenableWorker): ForegroundInfo =
    createRefreshForegroundInfo(context, worker, context.getString(R.string.vpn_refresh), NOTIFICATION_REFRESH)

  fun createFlagRefreshNotification(context: Context, worker: ListenableWorker): ForegroundInfo =
    createRefreshForegroundInfo(context, worker, context.getString(R.string.flags_refresh), NOTIFICATION_REFRESH_FLAG)

  fun createVpnBookRefreshNotification(context: Context, worker: ListenableWorker): ForegroundInfo =
    createRefreshForegroundInfo(context, worker, context.getString(R.string.vpnbook_refresh), NOTIFICATION_REFRESH_VPNBOOK)

  private fun createRefreshForegroundInfo(context: Context, worker: ListenableWorker, title: String, notificationId: Int): ForegroundInfo = with(context) {
    val cancelIntent = WorkManager.getInstance(this).createCancelPendingIntent(worker.id)

    val builder = NotificationCompat.Builder(this, REFRESH_CHANNEL)
      .setOngoing(true)
      .setContentTitle(title)
      .setSmallIcon(R.drawable.ic_logo)
      .setProgress(100, 0, true)
      .setPriority(NotificationCompat.PRIORITY_LOW)
      .addAction(R.drawable.ic_baseline_cancel_24, getString(android.R.string.cancel), cancelIntent)

    ForegroundInfo(notificationId, builder.build())
  }

  fun createVpnRefreshFailedNotification(context: Context) : Unit = with(context) {
    val openAppIntent = Intent(this, Splash::class.java)
    val openApp = TaskStackBuilder.create(this).run {
      addNextIntentWithParentStack(openAppIntent)
      getPendingIntent(0, PendingIntentHelper.getSafeFlags())
    }

    val builder = NotificationCompat.Builder(this, REFRESH_CHANNEL)
      .setContentTitle(getString(R.string.vpn_refresh_failed))
      .setStyle(NotificationCompat.BigTextStyle().bigText(getString(R.string.vpn_refresh_failed_text)))
      .setSmallIcon(R.drawable.ic_logo_error)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setContentIntent(openApp)
      .setAutoCancel(true)

    NotificationManagerCompat.from(this).notify(NOTIFICATION_REFRESH_FAILED, builder.build())
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

  fun createVpnUserActionRequiredNotification(context: Context): Unit = with(context) {
    val startIntent = Intent(this, Main::class.java)
    val pendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
      addNextIntentWithParentStack(startIntent)
      getPendingIntent(0, PendingIntentHelper.getSafeFlags())
    }
    val builder = NotificationCompat.Builder(this, COMMON_ALERT_CHANNEL).apply {
      setContentTitle(getString(R.string.vpn_action_required_title))
      setStyle(NotificationCompat.BigTextStyle().bigText(getString(R.string.vpn_action_required_text)))
      setSmallIcon(R.drawable.ic_logo_error)
      setAutoCancel(true)
      setContentIntent(pendingIntent)
    }

    NotificationManagerCompat.from(this).notify(NOTIFICATION_VPN_ACTION_REQUIRED, builder.build())
  }

  fun createVpnServersEmptyNotification(context: Context): Unit = with(context) {
    val builder = NotificationCompat.Builder(this, COMMON_ALERT_CHANNEL).apply {
      setContentTitle(getString(R.string.notify_empty_servers))
      setContentText(getString(R.string.notify_empty_servers_text))
      setSmallIcon(R.drawable.ic_logo_error)
      setAutoCancel(true)
    }

    NotificationManagerCompat.from(this).notify(NOTIFICATION_VPN_SERVERS_EMPTY, builder.build())
  }

  fun createNoInternetNotification(context: Context): Unit = with(context) {
    val builder = NotificationCompat.Builder(this, COMMON_ALERT_CHANNEL).apply {
      setContentTitle(getString(R.string.notify_no_net))
      setContentText(getString(R.string.notify_no_net_text))
      setSmallIcon(R.drawable.ic_logo_error)
      setAutoCancel(true)
    }

    NotificationManagerCompat.from(this).notify(NOTIFICATION_NO_INTERNET, builder.build())
  }

  fun createAuthenticationFailedNotification(context: Context, serverName: String, serverIp: String): Unit = with(context) {
    val builder = NotificationCompat.Builder(this, COMMON_ALERT_CHANNEL).apply {
      setContentTitle(getString(R.string.notification_auth_fail))
      setStyle(NotificationCompat.BigTextStyle().bigText(getString(R.string.notification_auth_fail_text, serverName, serverIp)))
      setSmallIcon(R.drawable.ic_logo_error)
      setAutoCancel(true)
    }

    NotificationManagerCompat.from(this).notify(NOTIFICATION_AUTH_FAILED, builder.build())
  }

  fun cancelAuthenticationFailedNotification(context: Context) {
    cancel(context, NOTIFICATION_AUTH_FAILED)
  }

  private fun cancel(context: Context, id: Int) {
    NotificationManagerCompat.from(context).cancel(id)
  }

  private const val REFRESH_CHANNEL = "refresh_channel"
  private const val COMMON_ALERT_CHANNEL = "common_alert_channel"
  private const val UPDATE_CHANNEL = "refresh_update"

  private const val NOTIFICATION_REFRESH = 132
  private const val NOTIFICATION_REFRESH_FAILED = 133
  private const val NOTIFICATION_UPDATE = 134
  private const val NOTIFICATION_VPN_ACTION_REQUIRED = 135
  private const val NOTIFICATION_NO_INTERNET = 136
  private const val NOTIFICATION_AUTH_FAILED = 137
  private const val NOTIFICATION_REFRESH_FLAG = 138
  private const val NOTIFICATION_REFRESH_VPNBOOK = 139
  private const val NOTIFICATION_VPN_SERVERS_EMPTY = 140
}