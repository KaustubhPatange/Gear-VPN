package com.kpstv.composetest.extensions

import com.kpstv.composetest.ui.components.ConnectivityStatus
import com.kpstv.composetest.ui.helpers.VpnConnectionStatus
import kotlin.random.Random

fun getRandomInt(max: Int = 400, offset: Int = 150) = Random.nextInt(max) + offset

/*
fun VpnConnectionStatus.asConnectivityStatus(): ConnectivityStatus {
  return when(this) {
    is VpnConnectionStatus.Connected -> ConnectivityStatus.CONNECTED
    is VpnConnectionStatus.StopVpn -> ConnectivityStatus.CONNECTED

    is VpnConnectionStatus.Disconnected -> ConnectivityStatus.DISCONNECT

    is VpnConnectionStatus.NewConnection -> ConnectivityStatus.CONNECTING
    is VpnConnectionStatus.Reconnecting -> ConnectivityStatus.CONNECTING
    is VpnConnectionStatus.Authenticating -> ConnectivityStatus.CONNECTING
    is VpnConnectionStatus.GetConfig -> ConnectivityStatus.CONNECTING
    is VpnConnectionStatus.Waiting ->  ConnectivityStatus.CONNECTING
    is VpnConnectionStatus.NoNetwork -> ConnectivityStatus.CONNECTING
    is VpnConnectionStatus.NULL -> ConnectivityStatus.CONNECTING

    is VpnConnectionStatus.Unknown -> ConnectivityStatus.NONE
  }
}*/
