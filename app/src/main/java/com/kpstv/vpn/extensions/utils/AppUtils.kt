package com.kpstv.vpn.extensions.utils

import android.R
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.MediaStore
import android.util.TypedValue
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import es.dmoral.toasty.Toasty


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

  fun Context.getWindowBackground(out: TypedValue = TypedValue()): Drawable {
    theme.resolveAttribute(R.attr.windowBackground, out, true)
    return ContextCompat.getDrawable(this, out.resourceId) ?: ColorDrawable(out.data)
  }

  fun Activity.setEdgeToEdgeSystemUiFlags() {
    WindowCompat.setDecorFitsSystemWindows(window, false)
  }

  fun Context.launchUrl(url: String) {
    try {
      Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse(url)
        startActivity(this)
      }
    } catch (e: Exception) {
      Toasty.error(this, getString(com.kpstv.vpn.R.string.error_no_activity_url)).show()
    }
  }
}