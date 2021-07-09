package com.kpstv.composetest.data.db.repository

import com.kpstv.composetest.data.db.localized.VpnDao
import com.kpstv.composetest.data.helpers.OpenApiParser
import com.kpstv.composetest.data.models.VpnConfiguration
import com.kpstv.composetest.extensions.utils.DateUtils
import com.kpstv.composetest.extensions.utils.NetworkUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import java.util.*
import kotlin.math.min

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

    fun convert(list: List<VpnConfiguration>): List<VpnConfiguration> {
      return list.sortedByDescending { it.speed.toFloat() }.subList(0, min(list.size, 3)).map {
        it.copy(premium = true)
      }.union(list).distinctBy { it.ip }
    }

    val local = fetchFromLocal()
    val offsetDate = DateUtils.format(Calendar.getInstance().time).toLong()
    if (!forceNetwork && local.isNotEmpty() && offsetDate < local.first().expireTime) {
      emit(VpnLoadState.Completed(local))
      return@flow
    }

    // Parse from network
    openApiParser.parse(
      onNewConfigurationAdded = { configs ->
        emit(VpnLoadState.Loading(convert(configs)))
      },
      onComplete = {configs ->
        val final = convert(configs)
        emit(VpnLoadState.Completed(final))
        vpnDao.insertAll(final)
      }
    )
  }

  private suspend fun fetchFromLocal(): List<VpnConfiguration> {
    return vpnDao.getAll()
  }
}