package com.kpstv.composetest.extensions.utils

import android.content.Context
import android.net.Uri
import android.provider.MediaStore

object AppUtils {
  private val passwordRegex = ".".toRegex()

  fun String.asPassword(char: String = "*"): String {
    return replace(passwordRegex, char)
  }

  fun Uri.getFileName(context: Context): String? {
    val cursor = context.contentResolver.query(this, arrayOf(MediaStore.Files.FileColumns.DISPLAY_NAME), null, null, null)
    val index = cursor?.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME) ?: return null
    cursor.moveToFirst()
    cursor.getString(index).let { name ->
      cursor.close()
      return name
    }
  }
}