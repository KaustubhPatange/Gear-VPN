package com.kpstv.vpn.data.db.repository

import com.kpstv.vpn.data.db.localized.VpnDao
import com.kpstv.vpn.data.helpers.OpenApiParser
import com.kpstv.vpn.data.helpers.VpnBookParser
import com.kpstv.vpn.data.models.VpnConfiguration
import com.kpstv.vpn.extensions.utils.DateUtils
import com.kpstv.vpn.extensions.utils.NetworkUtils
import com.kpstv.vpn.extensions.utils.safeNetworkAccessor
import kotlinx.coroutines.flow.*
import okhttp3.internal.toImmutableList
import javax.inject.Inject
import javax.inject.Singleton
import java.util.*

sealed class VpnLoadState(open val configs: List<VpnConfiguration>) {
  data class Loading(override val configs: List<VpnConfiguration> = emptyList()) :
    VpnLoadState(configs)

  data class Completed(override val configs: List<VpnConfiguration>) : VpnLoadState(configs)
}

@Singleton
class VpnRepository @Inject constructor(
  private val vpnDao: VpnDao,
  networkUtils: NetworkUtils
) {
  private val openApiParser: OpenApiParser = OpenApiParser(networkUtils)
  private val vpnBookParser: VpnBookParser = VpnBookParser(networkUtils)

  fun fetch(forceNetwork: Boolean = false): Flow<VpnLoadState> = flow {
    safeNetworkAccessor {

      emit(VpnLoadState.Loading())

      val local = fetchFromLocal()
      val offsetDate = DateUtils.format(Calendar.getInstance().time).toLong()
      if (!forceNetwork && local.isNotEmpty() && offsetDate < local.first().expireTime) {
        // Get from local
        emit(VpnLoadState.Completed(local))
      } else {
        // Parse from network
        var vpnConfigs = listOf<VpnConfiguration>()

        openApiParser.parse(
          onNewConfigurationAdded = { configs ->
            vpnConfigs = vpnConfigs + configs
            emit(VpnLoadState.Loading(vpnConfigs))
          },
          onComplete = { configs ->
            vpnConfigs = vpnConfigs + configs
          }
        )

        vpnBookParser.parse(
          onNewConfigurationAdded = { configs ->
            vpnConfigs = vpnConfigs + configs
            emit(VpnLoadState.Loading(vpnConfigs))
          },
          onComplete = { configs ->
            vpnConfigs = vpnConfigs + configs
            emit(VpnLoadState.Completed(vpnConfigs))
            vpnDao.insertAll(vpnConfigs)
          }
        )

      }
    }
  }

  private suspend fun fetchFromLocal(): List<VpnConfiguration> {
    return vpnDao.getAll()
  }

  private operator fun List<VpnConfiguration>.plus(configs: List<VpnConfiguration>): List<VpnConfiguration> {
    val into = toMutableList()
    for (c in configs) {
      into.removeAll { it.ip == c.ip }
      into.add(c)
    }
    return into.sortedBy { it.country }.sortedByDescending { it.premium }.toImmutableList()
  }

}