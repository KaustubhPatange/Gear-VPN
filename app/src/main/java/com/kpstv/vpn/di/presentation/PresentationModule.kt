package com.kpstv.vpn.di.presentation

import androidx.savedstate.SavedStateRegistryOwner
import dagger.Module
import dagger.Provides

@Module
class PresentationModule {
  @Provides
  fun provideSaveStateRegistryOwner(savedStateRegistryOwner: SavedStateRegistryOwner) = savedStateRegistryOwner
}