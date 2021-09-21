package com.kpstv.vpn.extensions.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.MediaStore
import android.util.TypedValue
import android.util.TypedValue.TYPE_FIRST_COLOR_INT
import android.util.TypedValue.TYPE_LAST_COLOR_INT
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.kpstv.vpn.R
import com.kpstv.vpn.extensions.launchWithFallback
import com.kpstv.vpn.extensions.setDefaultPackage
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

  fun Context.getColorAttr(attr: Int, out: TypedValue = TypedValue()): Int {
    theme.resolveAttribute(attr, out, true)
    return if (out.type in TYPE_FIRST_COLOR_INT..TYPE_LAST_COLOR_INT) {
      if (out.resourceId != 0) ContextCompat.getColor(this, out.resourceId) else out.data
    } else {
      0
    }
  }

  fun Context.getWindowBackground(out: TypedValue = TypedValue()): Drawable {
    theme.resolveAttribute(android.R.attr.windowBackground, out, true)
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
      Toasty.error(this, getString(R.string.error_no_activity_url)).show()
    }
  }

  fun Context.launchUrlInApp(url: String) {
    val colorSchemeParams = CustomTabColorSchemeParams.Builder()
      .setToolbarColor(getColorAttr(R.attr.colorPrimary))
      .build()
    CustomTabsIntent.Builder()
      .setDefaultColorSchemeParams(colorSchemeParams)
      .setStartAnimations(this, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
      .build()
      .setDefaultPackage(this)
      .launchWithFallback(this, Uri.parse(url)) {
        launchUrl(url)
      }
  }
}