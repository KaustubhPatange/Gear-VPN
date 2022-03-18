package com.kpstv.vpn.extensions

import android.os.Build
import androidx.work.OneTimeWorkRequest
import androidx.work.OutOfQuotaPolicy

fun OneTimeWorkRequest.Builder.setExpeditedCompat(outOfQuotaPolicy: OutOfQuotaPolicy): OneTimeWorkRequest.Builder {
  return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    setExpedited(outOfQuotaPolicy)
  } else {
    this
  }
}