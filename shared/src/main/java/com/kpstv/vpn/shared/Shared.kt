package com.kpstv.vpn.shared

data class SharedVpnConfiguration(
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
)