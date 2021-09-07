package com.kpstv.vpn.ui.helpers

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStoreFile
import com.kpstv.vpn.data.models.VpnConfiguration
import com.kpstv.vpn.shared.SharedVpnConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

object Settings {

  private lateinit var dataStore: DataStore<Preferences>
  private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

  fun init(context: Context) {
    if (::dataStore.isInitialized) return // no twice initialization
    dataStore = PreferenceDataStoreFactory.create(
      produceFile = { context.preferencesDataStoreFile(SETTINGS_PB) },
      scope = scope
    )
  }

  // Filter Servers

  fun getFilterServer(): Flow<ServerFilter> = dataStore.data.map { preferences ->
    ServerFilter.valueOf(preferences[filterServerKey] ?: ServerFilter.All.name)
  }

  fun setFilterServer(serverFilter: ServerFilter) {
    scope.launch {
      dataStore.edit { prefs ->
        prefs[filterServerKey] = serverFilter.name
      }
    }
  }

  val DefaultFilterServer: ServerFilter = ServerFilter.All

  enum class ServerFilter {
    All, Premium, Free
  }

  // Filter Apps

  fun getDisallowedVpnApps(): Flow<Set<String>> = dataStore.data.map { preferences ->
    preferences[filterAppKey] ?: emptySet()
  }

  fun setDisallowedVpnApps(values: Set<String>) {
    scope.launch {
      dataStore.edit { prefs ->
        prefs[filterAppKey] = values
      }
    }
  }

  // Last VPN config
  // This is typically used by Quick Tile service for "Gear connect" feature

  fun getLastVpnConfig(): Flow<VpnConfig?> = dataStore.data.map { preferences ->
    val configJson = preferences[lastVpnConfigKey] ?: return@map null
    VpnConfigConverter.fromStringToVpnConfig(configJson)
  }

  fun setLastVpnConfig(value: VpnConfig) {
    scope.launch {
      dataStore.edit { prefs ->
        VpnConfigConverter.toStringFromVpnConfig(value)?.let { config ->
          prefs[lastVpnConfigKey] = config
        }
      }
    }
  }

  // Purchase

  fun getHasPurchased(): Flow<Boolean> = dataStore.data.map { preferences ->
    preferences[purchaseKey] ?: return@map false
  }

  fun setHasPurchased(value: Boolean) {
    scope.launch {
      dataStore.edit { prefs ->
        prefs[purchaseKey] = value
      }
    }
  }

  // Welcome screen

  fun getWelcomeScreenShown(version: Int): Flow<Boolean> = dataStore.data.map { preferences ->
    preferences[getWelcomeScreenKey(version)] ?: return@map false
  }

  fun setWelcomeScreenShown(version: Int, value: Boolean) {
    scope.launch {
      dataStore.edit { prefs ->
        prefs[getWelcomeScreenKey(version)] = value
      }
    }
  }

  private const val FILTER_SERVER = "filter_server"
  private const val FILTER_APPS = "filter_apps"
  private const val LAST_VPN_CONFIG = "last_vpn_config"
  private const val HAS_PURCHASED = "has_purchased"
  private const val FIRST_LAUNCH = "first_launch"
  private const val SETTINGS_PB = "settings"

  private val filterServerKey = stringPreferencesKey(FILTER_SERVER)
  private val filterAppKey = stringSetPreferencesKey(FILTER_APPS)
  private val lastVpnConfigKey = stringPreferencesKey(LAST_VPN_CONFIG)
  private val purchaseKey = booleanPreferencesKey(HAS_PURCHASED)
  private fun getWelcomeScreenKey(version: Int) = booleanPreferencesKey("$FIRST_LAUNCH$version")
}