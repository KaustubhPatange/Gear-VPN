package com.kpstv.vpn.extensions

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION

fun CustomTabsIntent.setDefaultPackage(context: Context): CustomTabsIntent {
  val packages = getCustomTabsPackages(context).mapNotNull { it.activityInfo.packageName }
  intent.setPackage(packages.firstOrNull())
  return this
}

fun CustomTabsIntent.launchWithFallback(context: Context, uri: Uri, fallback: () -> Unit) {
  if (intent.getPackage() == null) {
    fallback()
  } else {
    launchUrl(context, uri)
  }
}

// https://developer.chrome.com/docs/android/custom-tabs/integration-guide/#how-can-i-check-whether-the-android-device-has-a-browser-that-supports-custom-tab
private fun getCustomTabsPackages(context: Context): ArrayList<ResolveInfo> {
  val pm: PackageManager = context.packageManager
  // Get default VIEW intent handler.
  val activityIntent: Intent = Intent()
    .setAction(Intent.ACTION_VIEW)
    .addCategory(Intent.CATEGORY_BROWSABLE)
    .setData(Uri.fromParts("http", "", null))

  // Get all apps that can handle VIEW intents.
  val resolvedActivityList: List<ResolveInfo> = pm.queryIntentActivities(activityIntent, 0)
  val packagesSupportingCustomTabs: ArrayList<ResolveInfo> = ArrayList()
  for (info in resolvedActivityList) {
    val serviceIntent = Intent()
    serviceIntent.action = ACTION_CUSTOM_TABS_CONNECTION
    serviceIntent.setPackage(info.activityInfo.packageName)
    // Check if this package also resolves the Custom Tabs service.
    if (pm.resolveService(serviceIntent, 0) != null) {
      packagesSupportingCustomTabs.add(info)
    }
  }
  return packagesSupportingCustomTabs
}