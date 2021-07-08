package com.kpstv.composetest.extensions

import android.content.Context
import androidx.annotation.RequiresApi

@RequiresApi(23)
inline fun<reified T> Context.getSystemService(): T = getSystemService(T::class.java)