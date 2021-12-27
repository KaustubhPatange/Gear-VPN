package com.kpstv.vpn.data.db.repository

import com.kpstv.vpn.data.db.localized.VpnDao
import com.kpstv.vpn.data.helpers.VpnBookParser
import com.kpstv.vpn.data.helpers.VpnGateParser
import com.kpstv.vpn.data.models.VpnConfiguration
import com.kpstv.vpn.di.app.AppScope
import com.kpstv.vpn.extensions.utils.NetworkUtils
import com.kpstv.vpn.extensions.utils.safeNetworkAccessor
import com.kpstv.vpn.logging.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import okhttp3.internal.toImmutableList
import java.io.InterruptedIOException
import javax.inject.Inject

sealed class VpnLoadState(open val configs: List<VpnConfiguration>) {
  data class Loading(override val configs: List<VpnConfiguration> = emptyList()) :
    VpnLoadState(configs)

  data class Completed(override val configs: List<VpnConfiguration>) : VpnLoadState(configs)

  data class Empty(override val configs: List<VpnConfiguration> = emptyList()) :
    VpnLoadState(configs)

  data class Interrupt(override val configs: List<VpnConfiguration>) : VpnLoadState(configs)

  fun isError(): Boolean = this is Empty
}

@AppScope
class VpnRepository @Inject constructor(
  private val vpnDao: VpnDao,
  networkUtils: NetworkUtils
) {
  private val vpnGateParser: VpnGateParser = VpnGateParser(networkUtils)
  private val vpnBookParser: VpnBookParser = VpnBookParser(networkUtils)

  fun fetch(forceNetwork: Boolean = false): Flow<VpnLoadState> = flow {
    safeNetworkAccessor(excludeExceptions = arrayOf(InterruptedIOException::class)) {
      fetchVPN(forceNetwork = forceNetwork)
    }
  }

  private suspend fun FlowCollector<VpnLoadState>.fetchVPN(forceNetwork: Boolean) {
    emit(VpnLoadState.Loading())

    val local = fetchFromLocal()
    if (!forceNetwork && local.isNotEmpty() && !local.first().isExpired()) {
      // Get from local
      emit(VpnLoadState.Completed(local))
    } else {
      // Parse from network
      var vpnConfigs = listOf<VpnConfiguration>()

      try {
        vpnGateParser.parse(
          onNewConfigurationAdded = { configs ->
            vpnConfigs = merge(configs, vpnConfigs)
            emit(VpnLoadState.Loading(vpnConfigs))
          },
          onComplete = { configs ->
            vpnConfigs = merge(configs, vpnConfigs)
          }
        )

        vpnBookParser.parse(
          onNewConfigurationAdded = { configs ->
            vpnConfigs = merge(configs, vpnConfigs)
            emit(VpnLoadState.Loading(vpnConfigs))
          },
          onComplete = { configs ->
            vpnConfigs = merge(configs, vpnConfigs)
          }
        )
      } catch (e: InterruptedIOException) {
        Logger.d("Warning: OkHttp client interrupted")
        if (vpnConfigs.isEmpty())
          emit(VpnLoadState.Empty())
        else
          emit(VpnLoadState.Interrupt(vpnConfigs))
        return
      }

      if (vpnConfigs.isNotEmpty()) {
        emit(VpnLoadState.Completed(vpnConfigs))
        vpnDao.insertAll(vpnConfigs)
      } else {
        emit(VpnLoadState.Empty())
      }

    }
  }

  private suspend fun fetchFromLocal(): List<VpnConfiguration> {
    return vpnDao.getAll()
  }

  companion object {
    fun merge(from: List<VpnConfiguration>, to: List<VpnConfiguration>): List<VpnConfiguration> {
      val into = to.toMutableList()
      // does from.union(to).distinctBy { it.ip } but respects the order
      for (c in from) {
        into.removeAll { it.ip == c.ip }
        into.add(c)
      }
      return into.sortedBy { it.country }.sortedByDescending { it.premium }.toImmutableList()
    }
  }
}