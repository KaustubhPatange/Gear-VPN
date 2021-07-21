package com.kpstv.vpn.data.db.repository

import com.kpstv.vpn.data.db.localized.VpnDao
import com.kpstv.vpn.data.helpers.OpenApiParser
import com.kpstv.vpn.data.models.VpnConfiguration
import com.kpstv.vpn.extensions.utils.DateUtils
import com.kpstv.vpn.extensions.utils.NetworkUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import java.util.*

sealed class VpnLoadState(open val configs: List<VpnConfiguration>) {
  data class Loading(override val configs: List<VpnConfiguration> = emptyList()) : VpnLoadState(configs)
  data class Completed(override val configs: List<VpnConfiguration>) : VpnLoadState(configs)
}

@Singleton
class VpnRepository @Inject constructor(
  private val vpnDao: VpnDao,
  networkUtils: NetworkUtils
) {
  private val openApiParser: OpenApiParser = OpenApiParser(networkUtils)

  fun fetch(forceNetwork: Boolean = false): Flow<VpnLoadState> = flow {

    emit(VpnLoadState.Loading())

    val local = fetchFromLocal()
    val offsetDate = DateUtils.format(Calendar.getInstance().time).toLong()
    if (!forceNetwork && local.isNotEmpty() && offsetDate < local.first().expireTime) {
      emit(VpnLoadState.Completed(local))
      return@flow
    }

    // Parse from network
    openApiParser.parse(
      onNewConfigurationAdded = { configs ->
        emit(VpnLoadState.Loading(configs))
      },
      onComplete = { configs ->
        emit(VpnLoadState.Completed(configs))
        vpnDao.insertAll(configs)
      }
    )
  }

  private suspend fun fetchFromLocal(): List<VpnConfiguration> {
    return vpnDao.getAll()
  }
}