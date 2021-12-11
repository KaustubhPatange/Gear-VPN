package com.kpstv.vpn.ui.helpers

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.kpstv.vpn.R
import com.kpstv.vpn.extensions.utils.Notifications
import com.kpstv.vpn.ui.viewmodels.VpnViewModel
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.flow.collect

class VpnActivityHelper(private val activity: ComponentActivity) : VpnHelper(activity, activity.lifecycleScope) {
  private val vpnViewModel by activity.viewModels<VpnViewModel>()

  private val lifecycleObserver = object : DefaultLifecycleObserver {
    override fun onDestroy(owner: LifecycleOwner) {
      dispose()
      super.onDestroy(owner)
    }
  }

  private val vpnResultContract = activity.registerForActivityResult(VPNServiceContract()) { ok ->
    val currentServer = currentServer
    if (ok && currentServer != null) {
      connect(currentServer)
    } else {
      Toasty.error(activity, activity.getString(R.string.vpn_request_denied)).show()
      disconnect(showDialog = false)
    }
  }

  init {
    activity.lifecycle.addObserver(lifecycleObserver)
  }

  fun initializeAndObserve() = with(activity) {
    init()
    lifecycleScope.launchWhenCreated {
      vpnViewModel.connectionStatus.collect { state ->
        if (state is VpnConnectionStatus.Commands.StopVpn) {
          disconnect()
        }
        if (state is VpnConnectionStatus.Commands.ReconnectVpn) {
          if (reconnect()) {
            Toasty.info(activity, getString(R.string.vpn_reconnecting)).show()
          }
        }
        if (state is VpnConnectionStatus.Commands.NewConnection) {
          // new server
          connect(state.server)

          // dismiss auth failed notification
          Notifications.cancelAuthenticationFailedNotification(activity)
        }
        if (state is VpnConnectionStatus.AuthenticationFailed) {
          Toasty.error(activity, getString(R.string.vpn_auth_failed)).show()

          // show auth failed notification
          val server = currentServer ?: return@collect
          Notifications.createAuthenticationFailedNotification(activity, server.country, server.ip)
        }
        if (state is VpnConnectionStatus.Connected) {
          // save the last connected vpn config
          currentServer?.let { server ->
            vpnViewModel.changeServer(server)
            Settings.setLastVpnConfig(server)
          }
        }
      }
    }
  }

  override fun onConnectivityStatusChanged(status: VpnConnectionStatus) {
    super.onConnectivityStatusChanged(status)
    vpnViewModel.dispatchConnectionState(status)
  }

  override fun onRequestPermissionForVPN(intent: Intent) {
    vpnResultContract.launch(intent)
  }

  override fun onStartVpnFailed(exception: Exception) {
    exception.printStackTrace()
    vpnViewModel.dispatchConnectionState(VpnConnectionStatus.Disconnected())
    showVpnErrorDialog(activity, exception)
  }

  override fun onStopVpnFailed(exception: Exception) {
    exception.printStackTrace()
  }

  override fun onPrepareVpnFailed() {
    Toasty.info(activity, activity.getString(R.string.vpn_disconnect)).show()
  }

  override fun onConnectionTimeout() {
    if (vpnViewModel.connectionStatus.value !is VpnConnectionStatus.Connected) {
      disconnect(showDialog = false)
      Toasty.error(activity, activity.getString(R.string.vpn_timeout)).show()
    }
  }

  override fun onServiceConnected() {
    val server = currentServer ?: return
    vpnViewModel.changeServer(server)
    if (isConnected()) vpnViewModel.setPreConnectionStatus()
  }

  private class VPNServiceContract : ActivityResultContract<Intent, Boolean>() {
    override fun createIntent(context: Context, input: Intent): Intent {
      return input
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
      return resultCode == Activity.RESULT_OK
    }
  }

  private fun showVpnErrorDialog(context: Context, exception: Exception) {
    android.app.AlertDialog.Builder(context, R.style.blinkt_dialog)
      .setTitle(context.getString(R.string.dialog_vpn_profile_error))
      .setMessage(exception.message)
      .setPositiveButton(context.getString(R.string.alright), null)
      .show()
  }
}