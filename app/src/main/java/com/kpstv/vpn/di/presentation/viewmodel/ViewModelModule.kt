package com.kpstv.vpn.di.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.kpstv.vpn.di.presentation.viewmodel.AssistedSavedStateViewModelFactory
import com.kpstv.vpn.di.presentation.viewmodel.ViewModelKey
import com.kpstv.vpn.ui.viewmodels.AppSheetViewModel
import com.kpstv.vpn.ui.viewmodels.FlagViewModel
import com.kpstv.vpn.ui.viewmodels.ImportViewModel
import com.kpstv.vpn.ui.viewmodels.VpnViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {
  @Binds
  @IntoMap
  @ViewModelKey(AppSheetViewModel::class)
  abstract fun bindAppSheetViewModel(f: AppSheetViewModel.Factory) : AssistedSavedStateViewModelFactory<out ViewModel>

  @Binds
  @IntoMap
  @ViewModelKey(ImportViewModel::class)
  abstract fun bindImportViewModel(f: ImportViewModel.Factory) : AssistedSavedStateViewModelFactory<out ViewModel>

  @Binds
  @IntoMap
  @ViewModelKey(VpnViewModel::class)
  abstract fun bindVpnViewModel(f: VpnViewModel.Factory) : AssistedSavedStateViewModelFactory<out ViewModel>

  @Binds
  @IntoMap
  @ViewModelKey(FlagViewModel::class)
  abstract fun bindFlagViewModel(f: FlagViewModel.Factory) : AssistedSavedStateViewModelFactory<out ViewModel>
}