package com.kpstv.vpn.data.db.repository

import com.kpstv.vpn.data.api.VpnApi
import com.kpstv.vpn.data.models.VpnConfiguration
import com.kpstv.vpn.di.app.AppScope
import com.kpstv.vpn.extensions.utils.safeNetworkAccessor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import java.io.InterruptedIOException
import javax.inject.Inject
import javax.net.ssl.SSLPeerUnverifiedException

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
  private val vpnApi: VpnApi,
) {

  fun fetch(forceNetwork: Boolean = false): Flow<VpnLoadState> = flow {
    safeNetworkAccessor(excludeExceptions = arrayOf(InterruptedIOException::class, SSLPeerUnverifiedException::class)) {
      fetchVPN(forceNetwork = forceNetwork)
    }
  }

  private suspend fun FlowCollector<VpnLoadState>.fetchVPN(forceNetwork: Boolean) {
    emit(VpnLoadState.Loading())

    try {
      val response = vpnApi.getVpnConfigs(
        cacheControl = if (forceNetwork) VpnApi.FORCE_NETWORK else VpnApi.CACHE_NORMAL,
        limit = 50
      )
      if (response.data.isEmpty()) {
        throw ListEmptyException("List should not be empty")
      }
      emit(VpnLoadState.Completed(response.data))
    } /*catch(e: ListEmptyException) {
      emit(VpnLoadState.Interrupt)
    } */ catch(e: Exception) {
      emit(VpnLoadState.Empty())
    }
  }

  class ListEmptyException(msg: String? = null) : Exception(msg)
}