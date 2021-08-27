package com.kpstv.vpn.ui.helpers

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

data class AppPackage(val name: String, val packageName: String, val loadIcon: () -> Drawable?)

object AppsHelper {
  // This is a thread blocking call
  fun getListOfInstalledApps(context: Context): Sequence<AppPackage> = sequence {
    for (item in context.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)) {
      val label = item.loadLabel(context.packageManager).toString()
      if (item.packageName != context.packageName && !label.contains(".")
        && (item.className != null || item.flags and ApplicationInfo.FLAG_INSTALLED == ApplicationInfo.FLAG_INSTALLED)
      ) {
        yield(
          AppPackage(
            name = item.loadLabel(context.packageManager).toString(),
            packageName = item.packageName,
            loadIcon = { item.loadIcon(context.packageManager) }
          )
        )
      }
    }
  }
}