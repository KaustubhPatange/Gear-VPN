package com.kpstv.vpn.ui.viewmodels

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.kpstv.vpn.di.presentation.viewmodel.AssistedSavedStateViewModelFactory
import com.kpstv.vpn.ui.helpers.AppPackage
import com.kpstv.vpn.ui.helpers.AppsHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.internal.toImmutableList

class AppSheetViewModel @AssistedInject constructor(
  @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

  fun get(context: Context): Flow<List<AppPackage>> = flow {
    val list = arrayListOf<AppPackage>()
    AppsHelper.getListOfInstalledApps(context).sortedBy { it.name }.forEach { appPackage ->
      list.add(appPackage)
      emit(list.toImmutableList())
    }
  }

  override fun onCleared() {
    android.util.Log.e("AppSheetViewModel", "onCleared()")
    super.onCleared()
  }

  @AssistedFactory
  interface Factory : AssistedSavedStateViewModelFactory<AppSheetViewModel>
}

