package com.kpstv.vpn.extensions.utils

import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.jetbrains.annotations.NonNls
import timber.log.Timber

object Logger {

  fun init() {
    Timber.plant(CrashlyticsTree())
  }

  fun d(message: String, vararg args: Any?) {
    Timber.d(message, args)
  }

  fun w(t: Throwable?, @NonNls message: String?, vararg args: Any?) {
    Timber.w(t, message, args)
  }

  // More methods if needed

  // Firebase Crashlytics tree
  private class CrashlyticsTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
      FirebaseCrashlytics.getInstance().log("[$tag] - $message${t?.let { "\n${it.printStackTrace()}" }}")
    }
  }
}