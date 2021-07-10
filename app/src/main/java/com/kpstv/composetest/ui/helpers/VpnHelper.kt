package com.kpstv.composetest.ui.helpers

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.VpnService
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.kpstv.composetest.R
import com.kpstv.composetest.data.models.VpnConfiguration
import com.kpstv.composetest.ui.viewmodels.VpnViewModel
import de.blinkt.openvpn.DisconnectVPNActivity
import de.blinkt.openvpn.OpenVpnApi
import de.blinkt.openvpn.core.OpenVPNService
import de.blinkt.openvpn.core.OpenVPNThread
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.flow.collect

// Cross ported https://github.com/KaustubhPatange/Moviesy/blob/master/app/src/main/java/com/kpstv/yts/vpn/VPNHelper.kt
class VpnHelper(private val activity: ComponentActivity) {
  private val vpnViewModel by activity.viewModels<VpnViewModel>()

  private var isVpnStarted: Boolean = false
  private var currentServer: VpnConfiguration? = null
  private var currentConfig: String? = null

  private val lifecycleObserver = object : DefaultLifecycleObserver {
    override fun onDestroy(owner: LifecycleOwner) {
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
          OpenVPNService.setDefaultStatus()
        }
        if (state is VpnConnectionStatus.NewConnection) {
          // new server
          if (isVpnStarted) stopVpn()
          prepareVpn(state.server, state.server.config)
        }
      }
    }
  }

  private fun stopVpn(): Boolean {
    try {
      OpenVPNThread.stop()
      isVpnStarted = false
      return true
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return false
  }

  private fun prepareVpn(server: VpnConfiguration, config: String) {
    this.currentServer = server
    this.currentConfig = config
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
      val server = currentServer ?: throw Exception("Error: Server is null")
      val config = currentConfig ?: throw Exception("Error: Server config is null")
      OpenVpnApi.startVpn(activity, config, server.country, "vpn", "vpn")
      isVpnStarted = true
    } catch (e: Exception) {
      e.printStackTrace()
      Toasty.error(activity, activity.getString(R.string.vpn_error)).show()
    }
  }

  private val broadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
      if (context == null || intent == null) return
      vpnViewModel.dispatchConnectionState(intent.getStringExtra("state") ?: "")

      val duration = intent.getStringExtra("duration") ?: "00:00:00"
      val lastPacketReceive = intent.getStringExtra("lastPacketReceive") ?: "0"
      val bytesIn = intent.getStringExtra("byteIn") ?: " "
      val bytesOut = intent.getStringExtra("byteOut") ?: " "

      android.util.Log.e("VpnHelper", "Status: ${intent.getStringExtra("state")}")

      /*val detail = VPNViewModel.ConnectionDetail(
        duration = duration,
        lastPacketReceive = lastPacketReceive,
        bytesIn = bytesIn,
        bytesOut = bytesOut
      )
      vpnViewModel.dispatchConnectionDetail(detail)*/
    }
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

sealed class VpnConnectionStatus(open val color: Int) {
  data class NewConnection(override val color: Int = Color.YELLOW, val server: VpnConfiguration) : VpnConnectionStatus(color)
  data class Unknown(override val color: Int = Color.RED) : VpnConnectionStatus(color)
  data class Disconnected(override val color: Int = Color.RED) : VpnConnectionStatus(color)
  data class Connected(override val color: Int = Color.GREEN) : VpnConnectionStatus(color)
  data class Waiting(override val color: Int = Color.YELLOW) : VpnConnectionStatus(color)
  data class Authenticating(override val color: Int = Color.YELLOW) : VpnConnectionStatus(color)
  data class Reconnecting(override val color: Int = Color.YELLOW) : VpnConnectionStatus(color)
  data class GetConfig(override val color: Int = Color.YELLOW) : VpnConnectionStatus(color)
  data class NoNetwork(override val color: Int = Color.RED) : VpnConnectionStatus(color)
  data class StopVpn(override val color: Int = Color.TRANSPARENT) : VpnConnectionStatus(color)
  data class NULL(override val color: Int = Color.RED) : VpnConnectionStatus(color)
}