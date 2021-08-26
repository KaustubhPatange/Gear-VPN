package de.blinkt.openvpn

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.os.RemoteException
import android.text.TextUtils
import android.util.Log
import de.blinkt.openvpn.core.ConfigParser
import de.blinkt.openvpn.core.ConfigParser.ConfigParseError
import de.blinkt.openvpn.core.ProfileManager
import de.blinkt.openvpn.core.VPNLaunchHelper
import java.io.IOException
import java.io.StringReader

object OpenVpnApi {
  private val TAG = "OpenVpnApi"

  @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
  @Throws(
    RemoteException::class
  )
  fun startVpn(
    context: Context,
    configText: String?,
    country: String?,
    userName: String? = null,
    password: String? = null,
    disallowedApps: HashSet<String> = HashSet()
  ) {
    if (TextUtils.isEmpty(configText)) throw RemoteException("config is empty")
    startVpnInternal(context, configText, country, userName, password, disallowedApps)
  }

  @Throws(RemoteException::class)
  private fun startVpnInternal(
    context: Context,
    inlineConfig: String?,
    sCountry: String?,
    userName: String?,
    pw: String?,
    disallowedApps: HashSet<String>
  ) {
    val cp = ConfigParser()
    try {
      cp.parseConfig(StringReader(inlineConfig))
      val vp = cp.convertProfile() // Analysis.ovpn
      Log.d(
        TAG, """
   startVpnInternal: ==============$cp
   $vp
   """.trimIndent()
      )
      vp.mName = sCountry
      if (vp.checkProfile(context) != R.string.no_error_found) {
        throw RemoteException(context.getString(vp.checkProfile(context)))
      }
      vp.mProfileCreator = context.packageName
      vp.mUsername = userName
      vp.mPassword = pw
      if (disallowedApps.isNotEmpty()) {
        vp.mAllowedAppsVpnAreDisallowed = true
        vp.mAllowedAppsVpn = disallowedApps
      }
      ProfileManager.setTemporaryProfile(context, vp)
      VPNLaunchHelper.startOpenVpn(vp, context)
    } catch (e: IOException) {
      throw RemoteException(e.message)
    } catch (e: ConfigParseError) {
      throw RemoteException(e.message)
    }
  }
}