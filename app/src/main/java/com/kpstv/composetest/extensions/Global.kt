package com.kpstv.composetest.extensions

import com.kpstv.composetest.data.models.VpnConfiguration
import com.kpstv.composetest.ui.components.ConnectivityStatus
import com.kpstv.composetest.ui.helpers.VpnConnectionStatus
import com.kpstv.vpn.shared.SharedVpnConfiguration
import kotlin.random.Random

fun getRandomInt(max: Int = 400, offset: Int = 150) = Random.nextInt(max) + offset

fun VpnConfiguration.asShared() : SharedVpnConfiguration = SharedVpnConfiguration(
  country = country,
  countryFlagUrl = countryFlagUrl,
  ip = ip,
  sessions = sessions,
  upTime = upTime,
  speed = speed,
  config = config,
  score = score,
  expireTime = expireTime,
  username = username,
  password = password
)

fun SharedVpnConfiguration.asVpnConfig() : VpnConfiguration = VpnConfiguration(
  country = country,
  countryFlagUrl = countryFlagUrl,
  ip = ip,
  sessions = sessions,
  upTime = upTime,
  speed = speed,
  config = config,
  score = score,
  expireTime = expireTime,
  username = username,
  password = password
)