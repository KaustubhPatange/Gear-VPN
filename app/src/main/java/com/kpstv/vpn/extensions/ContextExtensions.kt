package com.kpstv.vpn.extensions

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

@RequiresApi(23)
inline fun<reified T> Context.getSystemService(): T = getSystemService(T::class.java)

fun Context.drawableFrom(@DrawableRes res: Int): Drawable? = ContextCompat.getDrawable(this, res)