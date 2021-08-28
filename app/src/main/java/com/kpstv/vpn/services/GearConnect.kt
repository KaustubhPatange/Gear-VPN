package com.kpstv.vpn.services

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import androidx.annotation.RequiresApi
import com.kpstv.vpn.R
import com.kpstv.vpn.data.db.localized.VpnDao
import com.kpstv.vpn.extensions.asVpnConfig
import com.kpstv.vpn.extensions.utils.FlagUtils
import com.kpstv.vpn.extensions.utils.Notifications
import com.kpstv.vpn.ui.helpers.Settings
import com.kpstv.vpn.ui.helpers.VpnConfig
import com.kpstv.vpn.ui.helpers.VpnConnectionStatus
import com.kpstv.vpn.ui.helpers.VpnServiceHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.N)
@AndroidEntryPoint
class GearConnect : TileService() {
  private var connectJob = SupervisorJob()
  private var serviceJob = SupervisorJob()

  private lateinit var vpnHelper: VpnServiceHelper

  @Inject
  lateinit var vpnDao: VpnDao

  override fun onCreate() {
    super.onCreate()
    Log.e("GearConnect", "onCreate")

    vpnHelper = VpnServiceHelper(this)
    vpnHelper.init()
  }

  override fun onStartListening() {
    vpnHelper.init()

    serviceJob = SupervisorJob()

    Log.e("GearConnect", "onStartListening")

    ensureTitleStatus(vpnHelper.connectionStatus.value)

    CoroutineScope(serviceJob + Dispatchers.IO).launch {
      vpnHelper.connectionStatus.collect { status ->
        Log.e("GearConnect", "Status: $status")
        ensureTitleStatus(status)
      }
    }
    CoroutineScope(serviceJob + Dispatchers.IO).launch {
      vpnHelper.commandState.collect { command ->
        Log.e("GearConnect", "Command: $command")
        when(command) {
          is VpnServiceHelper.Command.Reset -> ensureOriginalStatus()
          is VpnServiceHelper.Command.ServiceReconnection -> ensureConnectedStatus()
        }
      }
    }
  }

  override fun onTileAdded() {
    Log.e("GearConnect", "onTileAdded")
  }

  override fun onTileRemoved() {
    Log.e("GearConnect", "onTileRemoved")
  }

  override fun onClick() {
    Log.e("GearConnect", "onClick: ${vpnHelper.isConnected()}")

    connectJob = SupervisorJob()
    CoroutineScope(connectJob + Dispatchers.Main).launch {
      if (vpnHelper.isConnected()) {
        qsTile?.state = Tile.STATE_UNAVAILABLE
        qsTile?.updateTile()

        vpnHelper.disconnect(showDialog = false)
      } else {
        ensureConnectingStatus()
        performConnection()
      }
    }
  }

  override fun onStopListening() {
    connectJob.cancel()
    serviceJob.cancel()
  }

  private fun ensureTitleStatus(status: VpnConnectionStatus) {
    if (status is VpnConnectionStatus.Connected) {
      ensureConnectedStatus()
    }
    if (status is VpnConnectionStatus.Disconnected) {
      ensureOriginalStatus()
    }
  }

  private fun ensureConnectingStatus() {
    qsTile?.state = Tile.STATE_UNAVAILABLE
    qsTile?.label = getString(R.string.tile_gear_connecting)
    qsTile?.updateTile()
  }

  private fun ensureConnectedStatus() {
    val server = vpnHelper.currentServer
    if (server != null) {
      qsTile?.state = Tile.STATE_ACTIVE
      qsTile?.label = getString(
        R.string.tile_gear_connected,
        FlagUtils.getAsCountryShortForms(server.country),
        server.connectionType.name
      )
      qsTile?.updateTile()
    }
  }

  private fun ensureOriginalStatus() {
    qsTile?.state = Tile.STATE_INACTIVE
    qsTile?.label = getString(R.string.gear_connect)
    qsTile?.updateTile()
  }

  private suspend fun performConnection() {
    val config = Settings.getLastVpnConfig().firstOrNull()
    if (config != null && !config.isExpired()) {
      vpnHelper.connect(config)
      return
    }

    val localConfigurations = vpnDao.getAll()
    if (config != null && config.isExpired()) {
      if (localConfigurations.isNotEmpty()) {
        val localConfig =
          localConfigurations.random().asVpnConfig(connectionType = VpnConfig.ConnectionType.TCP)
        vpnHelper.connect(localConfig)
        return
      } else {
        showUserActionRequired()
      }
    } else {
      showUserActionRequired()
    }
  }

  private fun showUserActionRequired() {
    Notifications.createVpnUserActionRequiredNotification(this)
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    Log.e("GearConnect", "onStartCommand: $flags, $startId")
    return super.onStartCommand(intent, flags, startId)
  }

  override fun onDestroy() {
    vpnHelper.dispose()
    Log.e("GearConnect", "onDestroy")

    super.onDestroy()
  }

}