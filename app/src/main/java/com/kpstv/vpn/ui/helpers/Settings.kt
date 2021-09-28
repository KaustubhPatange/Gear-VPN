package com.kpstv.vpn.ui.helpers

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStoreFile
import com.kpstv.vpn.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File

object Settings {

  private var dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
    produceFile = { File.createTempFile("", null) } // just for sake of seeing previews as I don't want lateinit
  )
  private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

  fun init(context: Context) {
    dataStore = PreferenceDataStoreFactory.create(
      produceFile = { context.preferencesDataStoreFile(SETTINGS_PB) },
      scope = scope
    )
  }

  // Filter Servers

  @Composable
  fun getFilterServer(): State<ServerFilter> = dataStore.data.map { preferences ->
    ServerFilter.valueOf(preferences[filterServerKey] ?: ServerFilter.All.name)
  }.collectAsState(initial = DefaultFilterServer)

  fun setFilterServer(serverFilter: ServerFilter) {
    scope.launch {
      dataStore.edit { prefs ->
        prefs[filterServerKey] = serverFilter.name
      }
    }
  }

  private val DefaultFilterServer: ServerFilter = ServerFilter.All

  enum class ServerFilter {
    All, Premium, Free
  }

  // Filter Apps

  object DisallowedVpnApps : Setting<Set<String>>(dataStore, default = emptySet()) {
    override val name: String = FILTER_APPS
    fun set(values: Set<String>) {
      scope.launch { setAsync(values) }
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

  object HasPurchased : Setting<Boolean>(dataStore, default = false) {
    override val name: String = HAS_PURCHASED
    fun set(value: Boolean) {
      scope.launch { setAsync(value) }
    }
  }

  // Version update

  fun getIfVersionUpdateChecked(version: Int): Flow<Boolean> = dataStore.data.map { preferences ->
    preferences[getFirstLaunchKey(version)] ?: return@map false
  }

  fun setIfVersionUpdateChecked(version: Int, value: Boolean) {
    scope.launch {
      dataStore.edit { prefs ->
        prefs[getFirstLaunchKey(version)] = value
      }
    }
  }

  // First Launch helper (include version updates as well)

  /**
   * Call once as value will be immediately changed when invoked first time.
   */
  suspend fun isFirstLaunchAndSet(): Boolean {
    val version = BuildConfig.VERSION_CODE * 1000
    val isUpgradeChecked = getIfVersionUpdateChecked(version).first()
    if (!isUpgradeChecked) {
      setIfVersionUpdateChecked(version, true)
    }
    return !isUpgradeChecked
  }

  // Server Quick Tip

  object ServerQuickTipShown : Setting<Boolean>(dataStore, default = false) {
    fun set(value: Boolean) {
      scope.launch { setAsync(value) }
    }
  }

  // Import server Tip

  object ImportServerTipShown : Setting<Boolean>(dataStore, default = false) {
    fun set(value: Boolean) {
      scope.launch { setAsync(value) }
    }
  }

  abstract class Setting<T : Any>(private val dataStore: DataStore<Preferences>, private val default: T) {
    open val name: String get() = this::class.javaObjectType.name
    open fun keyProvider(): Preferences.Key<out Any> {
      return when(default) {
        is Boolean -> booleanPreferencesKey(name)
        is String -> stringPreferencesKey(name)
        is Int -> intPreferencesKey(name)
        is Set<*> -> stringSetPreferencesKey(name)
        else -> throw IllegalStateException("Cannot create key for type ${default::class.javaObjectType.name}")
      }
    }
    @Suppress("UNCHECKED_CAST")
    open fun get(): Flow<T> {
      return dataStore.data.map { preferences -> preferences[keyProvider()] ?: default } as Flow<T>
    }
    @Suppress("UNCHECKED_CAST")
    open suspend fun setAsync(value: T) {
      dataStore.edit { prefs ->
        prefs[keyProvider() as Preferences.Key<T>] = value
      }
    }

    @Composable
    open fun getAsState(defaultValue: T = default): State<T> {
      return get().collectAsState(initial = defaultValue)
    }
  }

  private const val FILTER_SERVER = "filter_server"
  private const val FILTER_APPS = "filter_apps"
  private const val LAST_VPN_CONFIG = "last_vpn_config"
  private const val HAS_PURCHASED = "has_purchased"
  private const val FIRST_LAUNCH = "first_launch"
  private const val SETTINGS_PB = "settings"

  private val filterServerKey = stringPreferencesKey(FILTER_SERVER)
  private val lastVpnConfigKey = stringPreferencesKey(LAST_VPN_CONFIG)
  private fun getFirstLaunchKey(version: Int) = booleanPreferencesKey("$FIRST_LAUNCH$version")
}