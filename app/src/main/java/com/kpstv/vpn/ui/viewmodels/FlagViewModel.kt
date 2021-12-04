package com.kpstv.vpn.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.kpstv.vpn.data.db.localized.FlagDao
import com.kpstv.vpn.di.presentation.viewmodel.AssistedSavedStateViewModelFactory
import com.kpstv.vpn.extensions.utils.FlagUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.*

class FlagViewModel @AssistedInject constructor(
  private val flagDao: FlagDao,
  @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

  fun getFlagUrlByCountry(country: String): Flow<String> =
    flagDao.getByCountryFlow(country)
      .map { it?.flagUrl ?: FlagUtils.getOrNull(country) ?: "" }

  @AssistedFactory
  interface Factory : AssistedSavedStateViewModelFactory<FlagViewModel>
}