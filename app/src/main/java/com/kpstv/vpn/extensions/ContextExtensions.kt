package com.kpstv.vpn.extensions

import android.content.Context
import android.content.ContextWrapper
import android.graphics.drawable.Drawable
import androidx.activity.ComponentActivity
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

@RequiresApi(23)
inline fun<reified T> Context.getSystemService(): T = getSystemService(T::class.java)

fun Context.drawableFrom(@DrawableRes res: Int): Drawable? = ContextCompat.getDrawable(this, res)

fun Context.findActivity(): ComponentActivity {
  if (this is ComponentActivity) return this
  if (this is ContextWrapper) {
    val baseContext = this.baseContext
    if (baseContext is ComponentActivity) return baseContext
    return baseContext.findActivity()
  }
  throw NotImplementedError("Could not find activity from $this.")
}