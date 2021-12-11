package com.kpstv.vpn.ui.helpers

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object ReviewSettings {

  private const val VPN_CONNECT_TOTAL_COUNT = 12

  suspend fun canShowDialog(): Boolean {
    return !DoNotShowDialog.get().first() && (VpnConnectCount.get().first() >= VPN_CONNECT_TOTAL_COUNT)
  }

  fun doNotShowDialogAgain() {
    DoNotShowDialog.set(true)
  }

  suspend fun incrementVpnConnectCount() {
    VpnConnectCount.incrementAsync()
  }

  fun resetAll() {
    VpnConnectCount.reset()
  }

  private object VpnConnectCount : Settings.Setting<Int>(Settings.store, default = 0) {
    suspend fun incrementAsync(): Int {
      val increment = get().first() + 1
      setAsync(increment)
      return increment
    }

    fun reset() {
      scope.launch {
        setAsync(0)
      }
    }
  }

  private object DoNotShowDialog : Settings.Setting<Boolean>(Settings.store, default = false) {
    fun set(value: Boolean) {
      scope.launch { setAsync(value) }
    }
  }
}