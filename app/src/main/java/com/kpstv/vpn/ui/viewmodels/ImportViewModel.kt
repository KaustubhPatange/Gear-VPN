package com.kpstv.vpn.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kpstv.vpn.data.db.localized.LocalDao
import com.kpstv.vpn.data.models.LocalConfiguration
import com.kpstv.vpn.di.presentation.viewmodel.AssistedSavedStateViewModelFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ImportViewModel @AssistedInject constructor(
  @Assisted private val savedStateHandle: SavedStateHandle,
  private val localDao: LocalDao
) : ViewModel() {
  private val job = SupervisorJob()
  private val viewModelIOScope = CoroutineScope(job + Dispatchers.IO)

  val getConfigs: Flow<List<LocalConfiguration>> = localDao.getAsFlow()

  fun addConfig(config: LocalConfiguration) {
    viewModelScope.launch {
      localDao.insert(config)
    }
  }

  fun removeConfig(config: LocalConfiguration) {
    viewModelIOScope.launch {
      localDao.delete(config.id)
    }
  }

  override fun onCleared() {
    job.cancel()
    super.onCleared()
  }

  @AssistedFactory
  interface Factory : AssistedSavedStateViewModelFactory<ImportViewModel>
}