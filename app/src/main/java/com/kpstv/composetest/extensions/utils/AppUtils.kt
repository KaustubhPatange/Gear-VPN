package com.kpstv.composetest.extensions.utils

object AppUtils {
  private val passwordRegex = ".".toRegex()

  fun String.asPassword(char: String = "*"): String {
    return replace(passwordRegex, char)
  }
}