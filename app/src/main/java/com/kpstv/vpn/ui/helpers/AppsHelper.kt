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
      if (!label.contains(".") && item.flags != ApplicationInfo.FLAG_SYSTEM) {
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