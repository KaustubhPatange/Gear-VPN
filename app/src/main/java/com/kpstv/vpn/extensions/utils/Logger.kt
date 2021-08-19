package com.kpstv.vpn.extensions.utils

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kpstv.vpn.BuildConfig
import org.jetbrains.annotations.NonNls
import timber.log.Timber

object Logger {

  fun init() {
    if (BuildConfig.DEBUG) {
      Timber.plant(ExtendedDebugTree())
    }
    Timber.plant(CrashlyticsTree())
  }

  fun d(message: String, vararg args: Any?) {
    Timber.d(message, args)
  }

  fun w(t: Throwable?, @NonNls message: String?, vararg args: Any?) {
    Timber.w(t, message, args)
  }

  // More methods if needed

  private class ExtendedDebugTree : Timber.DebugTree() {

    private val fqcnIgnore = listOf(
      Timber::class.java.name,
      Timber.Forest::class.java.name,
      Timber.Tree::class.java.name,
      Timber.DebugTree::class.java.name,
      Logger::class.java.name,
      ExtendedDebugTree::class.java.name
    )

    private fun fetchTag(): String? {
      return Throwable().stackTrace
        .first { it.className !in fqcnIgnore }
        .let(::createStackElementTag)
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
      super.log(priority, fetchTag(), message, t)
    }
  }

  // Firebase Crashlytics tree
  private class CrashlyticsTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
      FirebaseCrashlytics.getInstance().log("[$tag] - $message${t?.let { "\n${it.printStackTrace()}" }}")
    }
  }
}