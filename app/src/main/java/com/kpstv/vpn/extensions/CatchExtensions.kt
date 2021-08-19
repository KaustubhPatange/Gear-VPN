package com.kpstv.vpn.extensions

import kotlin.reflect.KClass

/**
 * Take multiple Exceptions to catch and call the [catch] if it gets thrown. If the thrown Exception is not in the list
 * it will be just forwarded.
 * @see <a href="https://github.com/mark-kowalski/KotlinExtensions-Android/blob/master/kotlinsugar/src/main/java/dev/mko/kotlinsugar/KotlinSugar.kt">Source</a>
 */
inline fun multiCatch(run: () -> Unit, catch: (Throwable) -> Unit, vararg exceptions: KClass<out Throwable>) {
  try {
    run()
  } catch (exception: Exception) {
    val contains = exceptions.find {
      it.isInstance(exception)
    }
    if (contains != null) catch(exception)
    else throw exception
  }
}