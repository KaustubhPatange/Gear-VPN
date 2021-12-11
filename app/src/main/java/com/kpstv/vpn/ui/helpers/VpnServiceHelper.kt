package com.kpstv.vpn.ui.helpers

import android.app.Service
import android.content.Intent
import com.kpstv.vpn.extensions.utils.Notifications
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class VpnServiceHelper(private val service: Service) : VpnHelper(service) {

  private val connectionStatusStateFlow = MutableStateFlow<VpnConnectionStatus>(VpnConnectionStatus.Unknown())
  val connectionStatus: StateFlow<VpnConnectionStatus> = connectionStatusStateFlow.asStateFlow()

  private val commandsStateFlow = MutableStateFlow<Command>(Command.Reset)
  val commandState: StateFlow<Command> = commandsStateFlow.asStateFlow()

  private var initialized: Boolean = false
  override fun init() {
    if (!initialized) {
      super.init()
      initialized = true
    }
  }

  override fun dispose() {
    try {
      if (initialized) {
        super.dispose()
        initialized = false
      }
    } catch (e: Exception) { /* no-op */}
  }

  override fun onServiceConnected() {
    val server = currentServer ?: return
    commandsStateFlow.tryEmit(Command.ServiceReconnection(server))
  }

  override fun onConnectivityStatusChanged(status: VpnConnectionStatus) {
    super.onConnectivityStatusChanged(status)
    connectionStatusStateFlow.tryEmit(status)
  }

  override fun onConnectionTimeout() {
    commandsStateFlow.tryEmit(Command.Reset)
  }

  override fun onPrepareVpnFailed() {
    commandsStateFlow.tryEmit(Command.Reset)
  }

  override fun onStartVpnFailed(exception: Exception) {
    commandsStateFlow.tryEmit(Command.Reset)
  }

  override fun onStopVpnFailed(exception: Exception) {
    commandsStateFlow.tryEmit(Command.Reset)
  }

  override fun onRequestPermissionForVPN(intent: Intent) {
    Notifications.createVpnUserActionRequiredNotification(service)
    commandsStateFlow.tryEmit(Command.Reset)
  }

  sealed class Command {
    object Reset : Command()
    data class ServiceReconnection(val server: VpnConfig) : Command()
  }
}