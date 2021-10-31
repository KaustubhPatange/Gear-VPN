package com.kpstv.vpn.extensions.utils

import android.content.Context
import android.net.Uri
import java.text.DecimalFormat

object VpnUtils {

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

  fun formatVpnGateSpeed(value: String) : String {
    val formatter = DecimalFormat("#.##")
    val speed = value.replace(",", "").toDouble()
    return if (speed > 1000) {
      "${formatter.format((speed/1000.00))} Gbps"
    } else {
      "$value Mbps"
    }
  }
}