package com.kpstv.vpn.logging

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.jetbrains.annotations.NonNls
import timber.log.Timber

object Logger {

  @JvmSynthetic
  fun init(debug: Boolean) {
    if (debug) {
      Timber.plant(ExtendedDebugTree())
    }
    Timber.plant(CrashlyticsTree())
  }

  @JvmSynthetic
  fun disable(context: Context) {
    FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false)
    FirebaseAnalytics.getInstance(context).setAnalyticsCollectionEnabled(false)
  }

  @JvmStatic
  fun d(message: String, vararg args: Any?) {
    Timber.d(message, args)
  }

  @JvmStatic
  fun w(t: Throwable?, @NonNls message: String?, vararg args: Any?) {
    Timber.w(t, message, args)
  }

  // More methods if needed

  private open class ExtendedDebugTree : Timber.DebugTree() {

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
  private class CrashlyticsTree : ExtendedDebugTree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
      FirebaseCrashlytics.getInstance().log("[$tag] - $message${t?.let { "\n${it.printStackTrace()}" } ?: ""}")
    }
  }
}