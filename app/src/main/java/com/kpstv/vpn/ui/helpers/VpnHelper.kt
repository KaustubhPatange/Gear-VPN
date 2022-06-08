package com.kpstv.vpn.ui.helpers

import android.content.*
import android.graphics.Color
import android.net.VpnService
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.kpstv.vpn.data.models.VpnConfiguration
import com.kpstv.vpn.extensions.asShared
import com.kpstv.vpn.extensions.asVpnConfig
import com.kpstv.vpn.extensions.custom.AbstractMoshiConverter
import com.kpstv.vpn.logging.Logger
import com.squareup.moshi.JsonClass
import de.blinkt.openvpn.DisconnectVPNActivity
import de.blinkt.openvpn.OpenVpnApi
import de.blinkt.openvpn.core.OpenVPNService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.firstOrNull

// Some implementations are taken from
// https://github.com/KaustubhPatange/Moviesy/blob/master/app/src/main/java/com/kpstv/yts/vpn/VPNHelper.kt

open class VpnHelper(
  private val context: Context,
  private val lifecycleScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
  private var _currentServer: VpnConfig? = null
  var currentServer: VpnConfig?
    get() {
      val server = getSavedVpnConfigState()
      if (server != null) {
        return server
      }
      return _currentServer
    }
    private set(value) {
      _currentServer = value
      saveVpnConfigState()
    }

  private var openVpnService: OpenVPNService? = null
  private var serviceBinded: Boolean = false

  private var disposeJob = SupervisorJob()

  open fun init() {
    LocalBroadcastManager.getInstance(context)
      .registerReceiver(broadcastReceiver, IntentFilter("connectionState"))
    bindToVPNService()
  }

  open fun dispose() {
    LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver)
    openVpnService = null
    if (serviceBinded) {
      context.unbindService(serviceConnection)
    }
    serviceBinded = false
  }

  private fun saveVpnConfigState() {
    val service = openVpnService ?: return
    service.currentServer = currentServer?.asShared()
  }

  fun restoreVpnConfigState(): Boolean {
    val server = getSavedVpnConfigState()
    if (server != null) {
      currentServer = server
      return true
    }
    return false
  }

  /**
   * Called when the vpn connectivity status changes which is broadcasted from broadcast receiver.
   */
  open fun onConnectivityStatusChanged(status: VpnConnectionStatus) {
    if (status is VpnConnectionStatus.Connected) {
      lifecycleScope.launch {
        val total = ReviewSettings.incrementVpnConnectCount()
        Logger.d("Incrementing Connection count ($total)")
      }
    }
  }

  open fun onStartVpnFailed(exception: Exception) {}

  open fun onStopVpnFailed(exception: Exception) {}

  open fun onPrepareVpnFailed() {}

  open fun onRequestPermissionForVPN(intent: Intent) {}

  open fun onServiceConnected() {}

  /**
   * Called when there is VPN connection timeout
   */
  open fun onConnectionTimeout() {}

  fun isConnected(): Boolean {
    val service = openVpnService ?: return false
    return service.isConnected
  }

  fun hasConnectionStarted() : Boolean {
    val service = openVpnService ?: return false
    return service.hasConnectionStarted()
  }

  /**
   * Connect to [server] otherwise will use [currentServer].
   */
  fun connect(server: VpnConfig) {
    if (hasConnectionStarted()) stopVpn()
    prepareVpn(server)
  }

  fun disconnect(showDialog: Boolean = true) {
    if (showDialog) {
      context.startActivity(Intent(context, DisconnectVPNActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
      })
    } else {
      stopVpn()
    }
  }

  /**
   * @return True is reconnecting is in progress
   */
  suspend fun reconnect(): Boolean {
    val currentServer = currentServer
    if (hasConnectionStarted() && currentServer != null) {
      // filthy hack to reconnect vpn
      stopVpn()
      delay(1000)
      connect(currentServer)
      return true
    }
    return false
  }

  private fun stopVpn(): Boolean {
    try {
      openVpnService?.stopVPN(false)
      return true
    } catch (e: Exception) {
      onStopVpnFailed(e)
    }
    return false
  }

  private fun prepareVpn(server: VpnConfig) {
    this.currentServer = server
    if (!hasConnectionStarted()) {
      val intent = VpnService.prepare(context)
      if (intent != null) {
        onRequestPermissionForVPN(intent)
      } else startVpn()
    } else if (stopVpn()){
      onPrepareVpnFailed()
    }
  }

  private fun startVpn() {
    try {
      val server = currentServer ?: throw Exception("Server is null")
      if (server.config.isEmpty()) throw Exception("Config is empty")
      lifecycleScope.launch {
        val disallowedApps = Settings.DisallowedVpnApps.get().firstOrNull()
        OpenVpnApi.startVpn(
          context = context,
          configText = server.config,
          country = server.country,
          userName = server.username,
          password = server.password,
          disallowedApps = disallowedApps?.toHashSet() ?: HashSet()
        )
      }
      disposeAfterTimeout()
    } catch (e: Exception) {
      onStartVpnFailed(e)
    }
  }

  private val broadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
      if (context == null || intent == null) return
      val state = intent.getStringExtra("state") ?: ""

      val connectionStatus = VpnConnectionStatus.mapToVpnConnectionStatus(state)

      onConnectivityStatusChanged(connectionStatus)

      when (connectionStatus) {
        is VpnConnectionStatus.Disconnected -> {
          openVpnService?.setDefaultStatus()
          disposeJob.cancel()
        }
        is VpnConnectionStatus.Connected -> {
          bindToVPNService()
          disposeJob.cancel()
        }
        else -> {
        }
      }

      /*val duration = intent.getStringExtra("duration") ?: "00:00:00"
      val lastPacketReceive = intent.getStringExtra("lastPacketReceive") ?: "0"
      val bytesIn = intent.getStringExtra("byteIn") ?: " "
      val bytesOut = intent.getStringExtra("byteOut") ?: " "*/
    }
  }

  private fun disposeAfterTimeout() {
    disposeJob.cancel()
    disposeJob = SupervisorJob()
    CoroutineScope(lifecycleScope.coroutineContext + disposeJob).launch {
      delay(60 * 1000L)
      onConnectionTimeout()
    }
  }

  private fun bindToVPNService() {
    if (openVpnService == null) {
      val serviceIntent = Intent(context, OpenVPNService::class.java).apply {
        action = OpenVPNService.START_SERVICE
      }
      context.bindService(serviceIntent, serviceConnection, 0)
    }
  }

  private val serviceConnection = object : ServiceConnection {
    override fun onServiceConnected(name: ComponentName?, binder: IBinder) {
      serviceBinded = true
      openVpnService = (binder as OpenVPNService.LocalBinder).service
      restoreVpnConfigState()
      onServiceConnected()
      saveVpnConfigState()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
      serviceBinded = false
      openVpnService = null
      context.unbindService(this)
    }
  }

  private fun getSavedVpnConfigState(): VpnConfig? {
    val service = openVpnService ?: return null
    if (service.currentServer != null) {
      return service.currentServer.asVpnConfig()
    }
    return null
  }
}

@JsonClass(generateAdapter = true)
data class VpnConfig(
  val username: String?,
  val password: String?,
  val config: String,
  val country: String,
  val ip: String,
  val expireTime: Long,
  val connectionType: ConnectionType
) {

  @JsonClass(generateAdapter = false)
  enum class ConnectionType { Unknown, TCP, UDP }

  fun isNotEmpty(): Boolean {
    return config.isNotEmpty() && country.isNotEmpty()
  }

  fun isExpired(): Boolean = VpnConfiguration.isExpired(expireTime)

  object Converter : AbstractMoshiConverter<VpnConfig>(VpnConfig::class)

  companion object {
    fun createEmpty(): VpnConfig = VpnConfig(
      username = "",
      password = "",
      config = "",
      country = "Unknown",
      ip = "Unknown",
      expireTime = -1L,
      connectionType = ConnectionType.Unknown
    )
  }
}

sealed class VpnConnectionStatus(open val color: Int) {

  data class Unknown(override val color: Int = Color.RED) : VpnConnectionStatus(color)
  data class Disconnected(override val color: Int = Color.RED) : VpnConnectionStatus(color)
  data class Connected(override val color: Int = Color.GREEN) : VpnConnectionStatus(color)
  data class Waiting(override val color: Int = Color.YELLOW) : VpnConnectionStatus(color)
  data class Authenticating(override val color: Int = Color.YELLOW) : VpnConnectionStatus(color)
  data class AuthenticationFailed(override val color: Int = Color.YELLOW) :
    VpnConnectionStatus(color)

  data class Reconnecting(override val color: Int = Color.YELLOW) : VpnConnectionStatus(color)
  data class GetConfig(override val color: Int = Color.YELLOW) : VpnConnectionStatus(color)
  data class NoNetwork(override val color: Int = Color.RED) : VpnConnectionStatus(color)
  data class NULL(override val color: Int = Color.RED) : VpnConnectionStatus(color)

  // commands
  sealed class Commands {
    data class NewConnection(override val color: Int = Color.YELLOW, val server: VpnConfig) :
      VpnConnectionStatus(color)

    data class StopVpn(override val color: Int = Color.TRANSPARENT) : VpnConnectionStatus(color)
    data class ReconnectVpn(override val color: Int = Color.TRANSPARENT) :
      VpnConnectionStatus(color)
  }

  companion object {
    fun mapToVpnConnectionStatus(state: String): VpnConnectionStatus {
      return when (state) {
        "DISCONNECTED" -> Disconnected()
        "CONNECTED" -> Connected()
        "WAIT" -> Waiting()
        "AUTH" -> Authenticating()
        "RECONNECTING" -> Reconnecting()
        "NONETWORK" -> NoNetwork()
        "GET_CONFIG" -> GetConfig()
        "AUTH_FAILED" -> AuthenticationFailed()
        "null" -> NULL()
        "NOPROCESS" -> NULL()
        "VPN_GENERATE_CONFIG" -> NULL()
        "TCP_CONNECT" -> NULL()
        else -> Unknown()
      }
    }
  }
}