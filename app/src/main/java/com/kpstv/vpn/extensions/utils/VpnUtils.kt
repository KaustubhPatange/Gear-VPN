package com.kpstv.vpn.extensions.utils

import android.content.Context
import android.net.Uri
import java.text.DecimalFormat

object VpnUtils {

  private val commandsRegex = "client|dev tun3?|remote".toRegex()
  private val certificateRegex = "[-]+BEGIN\\sCERTIFICATE[-]+".toRegex()
  private val certificatePrivateRegex = "[-]+BEGIN\\s(RSA\\s)?PRIVATE\\sKEY[-]+".toRegex()

  fun verifyConfigData(context: Context, uri: Uri): Boolean {
    try {
      val stream = context.contentResolver.openInputStream(uri) ?: return false

      val lines = stream.bufferedReader().readLines()
      stream.close()

      // OPEN VPN uses tcp or udp protocol
      if (lines.none { it.matches(commandsRegex) }) return false

      // check for certificates
      if (lines.none { it.matches(certificateRegex) }) return false

      // no need to verify private key
//      if (lines.none { it.matches(certificatePrivateRegex) }) return false

      return true
    } catch (e: Exception) {
      return false
    }
  }

  fun formatVpnGateSpeed(speed: Float) : String {
    val formatter = DecimalFormat("#.##")
    return if (speed > 1000) {
      "${formatter.format((speed/1000.00))} Gbps"
    } else {
      "$speed Mbps"
    }
  }
}