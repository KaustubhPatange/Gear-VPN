package com.kpstv.composetest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.core.view.WindowCompat
import androidx.lifecycle.coroutineScope
import com.google.accompanist.insets.ProvideWindowInsets
import com.kpstv.composetest.extensions.utils.Initializer
import com.kpstv.composetest.ui.screens.NavigationScreen
import com.kpstv.composetest.ui.theme.ComposeTestTheme
import com.kpstv.navigation.compose.ComposeNavigator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  private lateinit var navigator: ComposeNavigator

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    WindowCompat.setDecorFitsSystemWindows(window, false)
    navigator = ComposeNavigator.with(this, savedInstanceState).initialize()

    Initializer.initialize(lifecycle.coroutineScope, this)

    setContent {
      ComposeTestTheme {
        ProvideWindowInsets {
          Surface(color = MaterialTheme.colors.background) {
            NavigationScreen(navigator = navigator)
          }
        }
      }
    }
  }
}