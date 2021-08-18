package com.kpstv.vpn.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "table_vpnconfigs")
data class VpnConfiguration(
  val country: String,
  val countryFlagUrl: String,
  val ip: String,
  /**
   * as "x sessions" for VPN Gate
   */
  val sessions: String,
  /**
   * as "x days/hours/mins" for VPN Gate
   */
  val upTime: String,
  val speed: String,
  val configTCP: String?,
  val configUDP: String?,
  val score: Long,
  val expireTime: Long,
  val username: String,
  val password: String,
  val premium: Boolean = false
) {
  @PrimaryKey(autoGenerate = true)
  var id: Int = 0

  companion object {
    fun createEmpty(): VpnConfiguration = VpnConfiguration(
      country = "Unknown",
      ip = "Unknown",
      countryFlagUrl = "",
      sessions = "",
      upTime = "",
      speed = "",
      configTCP = null,
      configUDP = null,
      username = "vpn",
      password = "vpn",
      score = 0,
      expireTime = 0
    )
  }
}