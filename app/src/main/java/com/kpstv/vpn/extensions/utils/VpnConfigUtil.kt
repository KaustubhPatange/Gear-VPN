package com.kpstv.vpn.extensions.utils

import android.content.Context
import android.net.Uri

object VpnConfigUtil {

  private val certificateRegex = "[-]+BEGIN\\sCERTIFICATE[-]+".toRegex()
  private val certificatePrivateRegex = "[-]+BEGIN\\s(RSA\\s)?PRIVATE\\sKEY[-]+".toRegex()

  fun verifyConfigData(context: Context, uri: Uri): Boolean {
    try {
      val stream = context.contentResolver.openInputStream(uri) ?: return false

      val lines = stream.bufferedReader().readLines()
      stream.close()

      // OPEN VPN uses tcp or udp protocol
      if (!(lines.contains("client") && (lines.contains("dev tun") || lines.contains("remote")))) {
        return false
      }

      // check for certificates
      if (lines.none { it.matches(certificateRegex) }) return false
      if (lines.none { it.matches(certificatePrivateRegex) }) return false

      return true
    } catch (e: Exception) {
      return false
    }
  }
}