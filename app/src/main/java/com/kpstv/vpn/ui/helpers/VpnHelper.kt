package com.kpstv.vpn.ui.helpers

import android.app.Activity
import android.content.*
import android.graphics.Color
import android.net.VpnService
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.kpstv.vpn.R
import com.kpstv.vpn.extensions.asShared
import com.kpstv.vpn.extensions.asVpnConfig
import com.kpstv.vpn.ui.viewmodels.VpnViewModel
import de.blinkt.openvpn.DisconnectVPNActivity
import de.blinkt.openvpn.OpenVpnApi
import de.blinkt.openvpn.core.OpenVPNService
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect

// Some implementations are taken from
// https://github.com/KaustubhPatange/Moviesy/blob/master/app/src/main/java/com/kpstv/yts/vpn/VPNHelper.kt

class VpnHelper(private val activity: ComponentActivity) {
  private val vpnViewModel by activity.viewModels<VpnViewModel>()

  private var isVpnStarted: Boolean = false
  private var currentServer: VpnConfig? = null

  private var openVpnService: OpenVPNService? = null

  private var disposeJob = SupervisorJob()

  private val lifecycleObserver = object : DefaultLifecycleObserver {
    override fun onStop(owner: LifecycleOwner) {
      val service = openVpnService ?: return
      service.currentServer = currentServer?.asShared()
      super.onPause(owner)
    }
    override fun onDestroy(owner: LifecycleOwner) {
      activity.unbindService(serviceConnection)
      LocalBroadcastManager.getInstance(activity).unregisterReceiver(broadcastReceiver)
      super.onDestroy(owner)
    }
  }

  private val vpnResultContract = activity.registerForActivityResult(VPNServiceContract()) { ok ->
    if (ok) { startVpn() }
  }

  init {
    activity.lifecycle.addObserver(lifecycleObserver)
  }

  fun initializeAndObserve() = with(activity) {
    LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, IntentFilter("connectionState"))
    lifecycleScope.launchWhenCreated {
      vpnViewModel.connectionStatus.collect { state ->
        if (state is VpnConnectionStatus.StopVpn) {
          activity.startActivity(Intent(activity, DisconnectVPNActivity::class.java))
        }
        if (state is VpnConnectionStatus.Disconnected) {
          openVpnService?.setDefaultStatus()
        }
        if (state is VpnConnectionStatus.NewConnection) {
          // new server
          if (isVpnStarted) stopVpn()
          prepareVpn(state.server)
        }
        if (state is VpnConnectionStatus.AuthenticationFailed) {
          Toasty.error(activity, getString(R.string.vpn_auth_failed)).show()
        }
        if (state is VpnConnectionStatus.Connected) {
          bindToVPNService()
          disposeJob.cancel()
        }
      }
    }

    bindToVPNService()
  }

  private fun stopVpn(): Boolean {
    try {
      openVpnService?.stopVPN(true)
//      OpenVPNThread.stopProcess()
      isVpnStarted = false
      return true
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return false
  }

  private fun prepareVpn(server: VpnConfig) {
    this.currentServer = server
    if (!isVpnStarted) {
      val intent = VpnService.prepare(activity)
      if (intent != null) {
        vpnResultContract.launch(intent)
      } else startVpn()
    } else if (stopVpn()) {
      Toasty.info(activity, activity.getString(R.string.vpn_disconnect)).show()
    }
  }

  private fun startVpn() {
    try {
      val server = currentServer ?: throw Exception("Server is null")
      if (server.config.isEmpty()) throw Exception("Config is empty")
      OpenVpnApi.startVpn(activity, server.config, server.country, server.username, server.password)
      isVpnStarted = true
      disposeAfterTimeout()
    } catch (e: Exception) {
      e.printStackTrace()
      Toasty.error(activity, activity.getString(R.string.vpn_error, e.message)).show()
    }
  }

  private val broadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
      if (context == null || intent == null) return
      vpnViewModel.dispatchConnectionState(intent.getStringExtra("state") ?: "")

      /*val duration = intent.getStringExtra("duration") ?: "00:00:00"
      val lastPacketReceive = intent.getStringExtra("lastPacketReceive") ?: "0"
      val bytesIn = intent.getStringExtra("byteIn") ?: " "
      val bytesOut = intent.getStringExtra("byteOut") ?: " "*/

//      android.util.Log.d("VpnHelper", "Status: ${intent.getStringExtra("state")}")
    }
  }

  private fun disposeAfterTimeout() {
    disposeJob.cancel()
    disposeJob = SupervisorJob()
    CoroutineScope(activity.lifecycleScope.coroutineContext + disposeJob).launch {
      delay(60 * 1000)
      if (vpnViewModel.connectionStatus.value !is VpnConnectionStatus.Connected) {
        stopVpn()
        Toasty.error(activity, activity.getString(R.string.vpn_timeout)).show()
      }
    }
  }

  private fun bindToVPNService() {
    val serviceIntent = Intent(activity, OpenVPNService::class.java).apply {
      action = OpenVPNService.START_SERVICE
    }
    activity.bindService(serviceIntent, serviceConnection, 0)
  }

  private val serviceConnection = object : ServiceConnection {
    override fun onServiceConnected(name: ComponentName?, binder: IBinder) {
      val service = (binder as OpenVPNService.LocalBinder).service
      if (service.currentServer != null) {
        val server = service.currentServer.asVpnConfig()
        currentServer = server

        vpnViewModel.changeServer(server)
        vpnViewModel.setPreConnectionStatus()
      }
      openVpnService = service
    }
    override fun onServiceDisconnected(name: ComponentName?) { openVpnService = null }
  }

  class VPNServiceContract : ActivityResultContract<Intent, Boolean>() {
    override fun createIntent(context: Context, input: Intent): Intent {
      return input
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
      return resultCode == Activity.RESULT_OK
    }
  }
}

data class VpnConfig(
  val username: String,
  val password: String,
  val config: String,
  val country: String,
  val ip: String,
  val connectionType: ConnectionType
) {
  enum class ConnectionType { Unknown, TCP, UDP }

  fun isNotEmpty(): Boolean {
    return config.isNotEmpty() && country.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty()
  }

  companion object {
    fun createEmpty() : VpnConfig = VpnConfig(
      username = "",
      password = "",
      config = "",
      country = "Unknown",
      ip = "Unknown",
      connectionType = ConnectionType.Unknown
    )
  }
}

sealed class VpnConnectionStatus(open val color: Int) {
  data class NewConnection(override val color: Int = Color.YELLOW, val server: VpnConfig) : VpnConnectionStatus(color)
  data class Unknown(override val color: Int = Color.RED) : VpnConnectionStatus(color)
  data class Disconnected(override val color: Int = Color.RED) : VpnConnectionStatus(color)
  data class Connected(override val color: Int = Color.GREEN) : VpnConnectionStatus(color)
  data class Waiting(override val color: Int = Color.YELLOW) : VpnConnectionStatus(color)
  data class Authenticating(override val color: Int = Color.YELLOW) : VpnConnectionStatus(color)
  data class AuthenticationFailed(override val color: Int = Color.YELLOW) : VpnConnectionStatus(color)
  data class Reconnecting(override val color: Int = Color.YELLOW) : VpnConnectionStatus(color)
  data class GetConfig(override val color: Int = Color.YELLOW) : VpnConnectionStatus(color)
  data class NoNetwork(override val color: Int = Color.RED) : VpnConnectionStatus(color)
  data class StopVpn(override val color: Int = Color.TRANSPARENT) : VpnConnectionStatus(color)
  data class NULL(override val color: Int = Color.RED) : VpnConnectionStatus(color)
}