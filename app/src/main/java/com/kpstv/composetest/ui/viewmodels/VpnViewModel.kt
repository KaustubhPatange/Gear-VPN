package com.kpstv.composetest.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kpstv.composetest.data.api.IpApi
import com.kpstv.composetest.data.db.repository.VpnLoadState
import com.kpstv.composetest.data.db.repository.VpnRepository
import com.kpstv.composetest.data.models.Location
import com.kpstv.composetest.data.models.VpnConfiguration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VpnViewModel @Inject constructor(
  private val savedStateHandle: SavedStateHandle,
  private val repository: VpnRepository,
  private val ipApi: IpApi
) : ViewModel() {
  private val publicIpStateFlow = MutableStateFlow<Location?>(null)
  val publicIp: StateFlow<Location?> = publicIpStateFlow.asStateFlow()

  private val currentVpnStateFlow = MutableStateFlow(VpnConfiguration.createEmpty())
  val currentVpn: StateFlow<VpnConfiguration> = currentVpnStateFlow.asStateFlow()

  init {
    viewModelScope.launch {
      publicIpStateFlow.emit(ipApi.fetch())
    }
  }

  fun fetchServers(forceNetwork: Boolean = false): Flow<VpnLoadState> =
    repository.fetch(forceNetwork = forceNetwork)
}