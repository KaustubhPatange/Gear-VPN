package com.kpstv.vpn.ui.activities

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import com.kpstv.vpn.App
import com.kpstv.vpn.di.presentation.PresentationModule
import com.kpstv.vpn.di.presentation.viewmodel.InjectingSavedStateViewModelFactory
import javax.inject.Inject

open class Dagger : ComponentActivity() {
  private val appComponent get() = (application as App).appComponent

  private val activityComponent by lazy {
    appComponent.newActivityComponentFactory().create(this)
  }

  private val presentationComponent by lazy {
    activityComponent.newPresentationComponentFactory()
      .create(this)
  }

  protected val injector get() = presentationComponent

  @Inject
  lateinit var abstractViewModelFactory: dagger.Lazy<InjectingSavedStateViewModelFactory>

  override val defaultViewModelProviderFactory get() = abstractViewModelFactory.get().create(this)

//  override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory =
//
//    abstractViewModelFactory.get().create(this)

}