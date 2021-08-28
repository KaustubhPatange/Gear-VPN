package com.kpstv.vpn.services

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.kpstv.vpn.ui.helpers.Settings
import com.kpstv.vpn.ui.helpers.VpnConfig
import de.blinkt.openvpn.core.OpenVPNService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.N)
class GearConnect : TileService() {
  private var openVpnService: OpenVPNService? = null
  private var isVpnStarted: Boolean = false

  private var currentServer: VpnConfig? = null

  var job = SupervisorJob()
  /* override fun onStartListening() {
     qsTile?.state = Tile.STATE_INACTIVE
     qsTile?.updateTile()
   }
 */
  override fun onClick() {
    job = SupervisorJob()
    CoroutineScope(job + Dispatchers.IO).launch {
      performConnection()
    }
  }

  override fun onStopListening() {
    job.cancel()
  }

  private suspend fun performConnection() {
    val config = Settings.getLastVpnConfig().firstOrNull()
    if (config == null) {

    }
  }

  private fun toggleServer() {
    if (isVpnStarted) {
      // stop connection
      try {
        openVpnService?.stopVPN(false)
        isVpnStarted = false
      } catch (e: Exception) {
        e.printStackTrace()
      }
    } else {
      val server = currentServer
      if (server != null) {
        // connect
      } else {
        // Toast to show error
      }
    }
  }

  private val serviceConnection = object: ServiceConnection {
    override fun onServiceConnected(name: ComponentName?, binder: IBinder) {
      val service = (binder as OpenVPNService.LocalBinder).service

      openVpnService = service
    }
    override fun onServiceDisconnected(name: ComponentName?) {
      openVpnService = null
      unbindService(this)
    }
  }

  private fun bindToVPNService() {
    val serviceIntent = Intent(this, OpenVPNService::class.java).apply {
      action = OpenVPNService.START_SERVICE
    }
    bindService(serviceIntent, serviceConnection, 0)
  }
}