package com.kpstv.composetest.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kpstv.composetest.data.db.localized.LocalDao
import com.kpstv.composetest.data.models.LocalConfiguration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImportViewModel @Inject constructor(
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

  override fun onCleared() {
    job.cancel()
    super.onCleared()
  }
}