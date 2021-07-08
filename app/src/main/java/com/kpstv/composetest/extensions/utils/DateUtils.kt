package com.kpstv.composetest.extensions.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
  private val dateFormatter = SimpleDateFormat("yyyyMMddHHmmss", Locale.US)

  fun format(date: Date): String = dateFormatter.format(date)

  fun formatExpireTime(expireTime: Long): String {
    return dateFormatter.parse(expireTime.toString())?.toGMTString() ?: expireTime.toString()
  }
}