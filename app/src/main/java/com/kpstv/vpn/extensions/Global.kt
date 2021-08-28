package com.kpstv.vpn.extensions

import com.kpstv.vpn.data.models.VpnConfiguration
import com.kpstv.vpn.shared.SharedVpnConfig
import com.kpstv.vpn.ui.helpers.VpnConfig
import kotlin.random.Random

fun getRandomInt(max: Int = 400, offset: Int = 150) = Random.nextInt(max) + offset

fun VpnConfig.asShared() : SharedVpnConfig = SharedVpnConfig(
  username = username,
  password = password,
  config = config,
  country = country,
  connectionType = connectionType.asShared(),
  expireTime = expireTime,
  ip = ip,
)

fun SharedVpnConfig.asVpnConfig() : VpnConfig = VpnConfig(
  username = username,
  password = password,
  config = config,
  country = country,
  connectionType = connectionType.asVpn(),
  expireTime = expireTime,
  ip = ip,
)

fun VpnConfiguration.asVpnConfig(connectionType: VpnConfig.ConnectionType) : VpnConfig = VpnConfig(
  username = username,
  password = password,
  config = when(connectionType) {
    VpnConfig.ConnectionType.UDP -> configUDP
    else -> configTCP
  } ?: "",
  country = country,
  ip = ip,
  expireTime = expireTime,
  connectionType = connectionType
)

private fun VpnConfig.ConnectionType.asShared() : SharedVpnConfig.ConnectionType {
  return when(this) {
    VpnConfig.ConnectionType.TCP -> SharedVpnConfig.ConnectionType.TCP
    VpnConfig.ConnectionType.UDP -> SharedVpnConfig.ConnectionType.UDP
    VpnConfig.ConnectionType.Unknown -> SharedVpnConfig.ConnectionType.UNKNOWN
  }
}

private fun SharedVpnConfig.ConnectionType.asVpn() : VpnConfig.ConnectionType {
  return when(this) {
    SharedVpnConfig.ConnectionType.TCP -> VpnConfig.ConnectionType.TCP
    SharedVpnConfig.ConnectionType.UDP -> VpnConfig.ConnectionType.UDP
    SharedVpnConfig.ConnectionType.UNKNOWN -> VpnConfig.ConnectionType.Unknown
  }
}