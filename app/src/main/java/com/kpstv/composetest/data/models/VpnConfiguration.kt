package com.kpstv.composetest.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "table_vpnconfigs")
data class VpnConfiguration(
  val country: String,
  val countryFlagUrl: String,
  val ip: String,
  val sessions: String,
  val upTime: String,
  val speed: String,
  val config: String,
  val score: Long,
  val expireTime: Long,
  val username: String,
  val password: String,
  val premium: Boolean = false
) {
  @PrimaryKey(autoGenerate = true)
  var id: Int = 0

  fun isNotEmpty(): Boolean {
    return config.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty()
  }

  companion object {
    fun createEmpty(): VpnConfiguration = VpnConfiguration(
      country = "Unknown",
      ip = "Unknown",
      countryFlagUrl = "",
      sessions = "",
      upTime = "",
      speed = "",
      config = "",
      username = "vpn",
      password = "vpn",
      score = 0,
      expireTime = 0
    )
  }
}