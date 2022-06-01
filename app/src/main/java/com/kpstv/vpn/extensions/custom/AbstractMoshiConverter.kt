package com.kpstv.vpn.extensions.custom

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import java.io.IOException
import kotlin.reflect.KClass

abstract class AbstractMoshiConverter<T : Any>(private val clazz: KClass<T>) {
  private val moshi = Moshi.Builder().build()

  @TypeConverter
  fun toString(data: T?): String? {
    return moshi.adapter(clazz.java).toJson(data)
  }

  @TypeConverter
  fun fromString(data: String?): T? {
    if (data == null) return null
    return try {
      moshi.adapter(clazz.java).fromJson(data)
    } catch (e: IOException) {
      e.printStackTrace()
      null
    }
  }
}