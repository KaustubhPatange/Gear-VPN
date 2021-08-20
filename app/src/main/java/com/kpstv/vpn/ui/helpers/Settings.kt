package com.kpstv.vpn.ui.helpers

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
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

  fun getFilterServer(): Flow<ServerFilter> = dataStore.data.map { preferences ->
    ServerFilter.valueOf(preferences[filterKey] ?: ServerFilter.All.name)
  }

  fun setFilterServer(serverFilter: ServerFilter) {
    scope.launch {
      dataStore.edit { prefs ->
        prefs[filterKey] = serverFilter.name
      }
    }
  }

  val DefaultFilterServer: ServerFilter = ServerFilter.All

  enum class ServerFilter {
    All, Premium, Free
  }

  private const val FILTER_SERVER = "filter_server"
  private const val SETTINGS_PB = "settings"

  private val filterKey = stringPreferencesKey(FILTER_SERVER)
}