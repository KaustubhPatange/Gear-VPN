package com.kpstv.vpn.ui.helpers

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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

  private const val FILTER_SERVER = "filter_server"
  private const val FILTER_APPS = "filter_apps"
  private const val SETTINGS_PB = "settings"

  private val filterServerKey = stringPreferencesKey(FILTER_SERVER)
  private val filterAppKey = stringSetPreferencesKey(FILTER_APPS)
}