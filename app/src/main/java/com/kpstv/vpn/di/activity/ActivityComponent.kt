package com.kpstv.vpn.di.activity

import androidx.activity.ComponentActivity
import com.kpstv.vpn.di.presentation.PresentationComponent
import com.kpstv.vpn.di.presentation.PresentationModule
import dagger.BindsInstance
import dagger.Subcomponent

@Subcomponent
interface ActivityComponent {

  fun newPresentationComponentFactory(): PresentationComponent.Factory

  @Subcomponent.Factory
  interface Factory {
    fun create(@BindsInstance activity: ComponentActivity): ActivityComponent
  }
}