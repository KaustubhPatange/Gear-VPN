package com.kpstv.vpn.extensions

import android.app.PendingIntent
import android.content.Intent
import android.os.Build

fun Intent.getPendingIntentFlags(): Int = if (Build.VERSION.SDK_INT >= 30) PendingIntent.FLAG_IMMUTABLE else 0 or PendingIntent.FLAG_CANCEL_CURRENT