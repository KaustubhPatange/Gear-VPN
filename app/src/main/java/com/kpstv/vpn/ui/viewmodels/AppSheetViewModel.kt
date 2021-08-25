package com.kpstv.vpn.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import com.kpstv.vpn.ui.helpers.AppPackage
import com.kpstv.vpn.ui.helpers.AppsHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.internal.toImmutableList
import javax.inject.Inject

@HiltViewModel
class AppSheetViewModel @Inject constructor() : ViewModel() {

  fun get(context: Context): Flow<List<AppPackage>> = flow {
    val list = arrayListOf<AppPackage>()
    AppsHelper.getListOfInstalledApps(context).sortedBy { it.name }.forEach { appPackage ->
      list.add(appPackage)
      emit(list.toImmutableList())
    }
  }

}

