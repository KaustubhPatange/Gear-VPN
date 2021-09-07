package com.kpstv.vpn.services

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.kpstv.vpn.R
import com.kpstv.vpn.data.db.localized.VpnDao
import com.kpstv.vpn.extensions.asVpnConfig
import com.kpstv.vpn.extensions.utils.FlagUtils
import com.kpstv.vpn.extensions.utils.NetworkMonitor
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
import kotlinx.coroutines.flow.combine
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
    vpnHelper = VpnServiceHelper(this)
    vpnHelper.init()
  }

  override fun onStartListening() {
    vpnHelper.init()

    serviceJob = SupervisorJob()

    ensureTitleStatus(
      isConnected = NetworkMonitor.connection.value,
      status = vpnHelper.connectionStatus.value
    )

    CoroutineScope(serviceJob + Dispatchers.IO).launch {
      vpnHelper.connectionStatus.combine(NetworkMonitor.connection) { status, isConnected -> status to isConnected }
        .collect { (status: VpnConnectionStatus, isConnected: Boolean) ->
          ensureTitleStatus(isConnected, status)
        }
    }
    CoroutineScope(serviceJob + Dispatchers.IO).launch {
      vpnHelper.commandState.collect { command ->
        when(command) {
          is VpnServiceHelper.Command.Reset -> ensureOriginalStatus()
          is VpnServiceHelper.Command.ServiceReconnection -> ensureConnectedStatus()
        }
      }
    }
  }

  override fun onClick() {
    connectJob = SupervisorJob()
    CoroutineScope(connectJob + Dispatchers.Main).launch {
      if (!NetworkMonitor.connection.value) {
        Notifications.createNoInternetNotification(this@GearConnect)
      } else if (vpnHelper.isConnected()) {
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

  private fun ensureTitleStatus(isConnected: Boolean, status: VpnConnectionStatus) {
    /*if (!isConnected && !vpnHelper.isConnected()) {
      ensureNotConnectedStatus()
    } else */if (status is VpnConnectionStatus.Connected || vpnHelper.isConnected()) {
      ensureConnectedStatus()
    } else if (status is VpnConnectionStatus.Disconnected) {
      ensureOriginalStatus()
    } else if (status !is VpnConnectionStatus.NULL && status !is VpnConnectionStatus.Unknown) {
      ensureConnectingStatus()
    }
  }

  private fun ensureConnectingStatus() {
    qsTile?.state = Tile.STATE_UNAVAILABLE
    qsTile?.label = getString(R.string.tile_gear_connecting)
    qsTile?.updateTile()
  }

  private fun ensureConnectedStatus() {
    val server = vpnHelper.currentServer
    if (server != null && vpnHelper.isConnected()) {
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

  private fun ensureNotConnectedStatus() {
    qsTile?.state = Tile.STATE_UNAVAILABLE
    qsTile?.label = getString(R.string.tile_gear_no_net)
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
    ensureOriginalStatus()
  }

  override fun onDestroy() {
    vpnHelper.dispose()
    super.onDestroy()
  }

}