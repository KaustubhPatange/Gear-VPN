package com.kpstv.vpn.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.kpstv.vpn.data.api.PlanApi
import com.kpstv.vpn.di.presentation.viewmodel.AssistedSavedStateViewModelFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class PlanViewModel @AssistedInject constructor(
  @Assisted private val savedStateHandle: SavedStateHandle,
  private val planApi: PlanApi
): ViewModel() {

  suspend fun fetchPlans() = planApi.fetchPlans()

  @AssistedFactory
  interface Factory : AssistedSavedStateViewModelFactory<PlanViewModel>
}