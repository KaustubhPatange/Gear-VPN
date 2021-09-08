package com.kpstv.vpn.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kpstv.vpn.data.api.IpApi
import com.kpstv.vpn.data.db.repository.VpnLoadState
import com.kpstv.vpn.data.db.repository.VpnRepository
import com.kpstv.vpn.data.models.Location
import com.kpstv.vpn.logging.Logger
import com.kpstv.vpn.ui.components.ConnectivityStatus
import com.kpstv.vpn.ui.helpers.VpnConfig
import com.kpstv.vpn.ui.helpers.VpnConnectionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VpnViewModel @Inject constructor(
  private val savedStateHandle: SavedStateHandle,
  private val repository: VpnRepository,
  private val ipApi: IpApi
) : ViewModel() {
  private val publicIpStateFlow = MutableStateFlow<Location?>(null)
  val publicIp: StateFlow<Location?> = publicIpStateFlow.asStateFlow()

  private val currentVpnStateFlow = MutableStateFlow(VpnConfig.createEmpty())
  val currentVpn: StateFlow<VpnConfig> = currentVpnStateFlow.asStateFlow()

  private val connectionStatusStateFlow: MutableStateFlow<VpnConnectionStatus> =
    MutableStateFlow(VpnConnectionStatus.Unknown())
  val connectionStatus: StateFlow<VpnConnectionStatus> = connectionStatusStateFlow.asStateFlow()

  private val connectivityStatusStateFlow = MutableStateFlow(ConnectivityStatus.NONE)
  val connectivityStatus: StateFlow<ConnectivityStatus> = connectivityStatusStateFlow.asStateFlow()

  init {
    viewModelScope.launch {
      try {
        val ipData = ipApi.fetch()
        publicIpStateFlow.emit(ipData)

        Logger.d("IP Info: $ipData")
      } catch (e: Exception) {
        Logger.w(e, "Error fetching IP: (No crash)")
      }
    }

    viewModelScope.launch {
      connectionStatus.collect { state ->
        when(state) {
          is VpnConnectionStatus.Unknown -> { }
          is VpnConnectionStatus.Commands.StopVpn -> { }
          is VpnConnectionStatus.Commands.ReconnectVpn -> { }
          is VpnConnectionStatus.NULL -> { }
          is VpnConnectionStatus.Reconnecting -> connectivityStatusStateFlow.emit(ConnectivityStatus.RECONNECTING)
          is VpnConnectionStatus.Disconnected -> {
            connectivityStatusStateFlow.emit(ConnectivityStatus.DISCONNECT)
          }
          is VpnConnectionStatus.Connected -> {
            connectivityStatusStateFlow.emit(ConnectivityStatus.CONNECTED)
          }
          else -> connectivityStatusStateFlow.emit(ConnectivityStatus.CONNECTING)
        }
      }
    }
    viewModelScope.launch {
      connectivityStatusStateFlow.collect { value ->
      }
    }
  }

  fun fetchServers(forceNetwork: Boolean = false): Flow<VpnLoadState> =
    repository.fetch(forceNetwork = forceNetwork)

  fun connect() {
    viewModelScope.launch {
      connectionStatusStateFlow.emit(VpnConnectionStatus.Commands.NewConnection(server = currentVpnStateFlow.value))
    }
  }

  fun disconnect() {
    viewModelScope.launch {
      connectionStatusStateFlow.emit(VpnConnectionStatus.Commands.StopVpn())
    }
  }

  fun reconnect() {
    viewModelScope.launch {
      connectionStatusStateFlow.emit(VpnConnectionStatus.Commands.ReconnectVpn())
    }
  }

  fun changeServer(config: VpnConfig) {
    viewModelScope.launch {
      currentVpnStateFlow.emit(config)
    }
  }

  fun setPreConnectionStatus() {
    viewModelScope.launch {
      connectionStatusStateFlow.emit(VpnConnectionStatus.Connected())
    }
  }

  fun dispatchConnectionState(status: VpnConnectionStatus) {
    connectionStatusStateFlow.tryEmit(status)
  }
}