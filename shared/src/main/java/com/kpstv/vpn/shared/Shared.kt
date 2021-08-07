package com.kpstv.vpn.shared

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SharedVpnConfig(
  val username: String,
  val password: String,
  val config: String,
  val country: String,
  val ip: String,
  val connectionType: ConnectionType
) : Parcelable {
  enum class ConnectionType { UNKNOWN, TCP, UDP }
}