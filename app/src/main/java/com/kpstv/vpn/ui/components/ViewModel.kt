package com.kpstv.vpn.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kpstv.vpn.extensions.findActivity
import com.kpstv.vpn.ui.activities.Dagger

// An extension to create ViewModel using activity's factory. Since we use dagger & assisted
// injection to construct ViewModels, we need an instance of that factory scoped to our the
// LocalSavedStateRegistryOwner to model SavedStateHandle to our ViewModel constructor.
@Composable
inline fun <reified VM : ViewModel> composeViewModel(): VM {
  val context = LocalContext.current
  val activity = remember { context.findActivity() as Dagger }
  val savedStateRegistryOwner = LocalSavedStateRegistryOwner.current
  val factory = remember { activity.abstractViewModelFactory.get().create(savedStateRegistryOwner) }
  return viewModel(factory = factory)
}