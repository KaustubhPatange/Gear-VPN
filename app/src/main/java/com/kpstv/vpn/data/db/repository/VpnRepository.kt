package com.kpstv.vpn.data.db.repository

import com.kpstv.vpn.data.db.localized.VpnDao
import com.kpstv.vpn.data.helpers.OpenApiParser
import com.kpstv.vpn.data.helpers.VpnBookParser
import com.kpstv.vpn.data.models.VpnConfiguration
import com.kpstv.vpn.extensions.utils.DateUtils
import com.kpstv.vpn.extensions.utils.NetworkUtils
import com.kpstv.vpn.extensions.utils.safeNetworkAccessor
import kotlinx.coroutines.flow.*
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

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
        val vpnConfigs = arrayListOf<VpnConfiguration>()

        openApiParser.parse(
          onNewConfigurationAdded = { configs ->
            mergeConfigs(configs, vpnConfigs)
            emit(VpnLoadState.Loading(vpnConfigs.toMutableList()))
          },
          onComplete = { configs ->
            mergeConfigs(configs, vpnConfigs)
          }
        )

        vpnBookParser.parse(
          onNewConfigurationAdded = { configs ->
            mergeConfigs(configs, vpnConfigs)
            emit(VpnLoadState.Loading(vpnConfigs.toMutableList()))
          },
          onComplete = { configs ->
            mergeConfigs(configs, vpnConfigs)
            emit(VpnLoadState.Completed(vpnConfigs.toMutableList()))
            vpnDao.insertAll(vpnConfigs)
          }
        )

      }
    }
  }

  private suspend fun fetchFromLocal(): List<VpnConfiguration> {
    return vpnDao.getAll()
  }

  private fun mergeConfigs(configs: List<VpnConfiguration>, into: ArrayList<VpnConfiguration>) {
    for (c in configs) {
      into.removeAll { it.ip == c.ip }
      into.add(c)
    }
    into.sortWith(compareBy { it.country })
    into.sortWith(compareByDescending { it.premium })
//    android.util.Log.e("mergeConfigs", "${into.map { "${it.country}-${it.ip}" }}")
  }
}