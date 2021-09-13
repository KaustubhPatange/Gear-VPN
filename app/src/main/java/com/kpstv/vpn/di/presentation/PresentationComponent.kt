package com.kpstv.vpn.di.presentation

import androidx.savedstate.SavedStateRegistryOwner
import com.kpstv.vpn.ui.activities.Main
import dagger.BindsInstance
import dagger.Subcomponent

@PresentationScope
@Subcomponent(modules = [PresentationModule::class, ViewModelModule::class])
interface PresentationComponent {
  fun inject(activity: Main)

  @Subcomponent.Factory
  interface Factory {
    fun create(@BindsInstance savedStateRegistryOwner: SavedStateRegistryOwner) : PresentationComponent
  }
}