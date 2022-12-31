package com.kpstv.vpn.ui.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.lifecycle.coroutineScope
import com.google.accompanist.insets.ProvideWindowInsets
import com.kpstv.navigation.compose.ComposeNavigator
import com.kpstv.vpn.extensions.CoilCustomImageLoader
import com.kpstv.vpn.extensions.SlideTopTransition
import com.kpstv.vpn.extensions.utils.AppUtils.setEdgeToEdgeSystemUiFlags
import com.kpstv.vpn.extensions.utils.Initializer
import com.kpstv.vpn.extensions.utils.NetworkMonitor
import com.kpstv.vpn.ui.helpers.BillingHelper
import com.kpstv.vpn.ui.helpers.InAppUpdates
import com.kpstv.vpn.ui.helpers.VpnActivityHelper
import com.kpstv.vpn.ui.screens.NavigationScreen
import com.kpstv.vpn.ui.theme.ComposeTestTheme
import com.kpstv.vpn.ui.viewmodels.PlanViewModel

class Main : Dagger() {
  private lateinit var navigator: ComposeNavigator

  private val planViewModel by viewModels<PlanViewModel>()

  private val vpnHelper by lazy { VpnActivityHelper(this) }
  private val billingHelper by lazy { BillingHelper(this, planViewModel) }
  private val updateHelper by lazy { InAppUpdates(this) }

  override fun onCreate(savedInstanceState: Bundle?) {
    injector.inject(this)
    super.onCreate(savedInstanceState)
    setEdgeToEdgeSystemUiFlags()
    navigator = ComposeNavigator.with(this, savedInstanceState)
      .registerTransitions(SlideTopTransition)
      .initialize()

    Initializer.initialize(lifecycle.coroutineScope, this)

    setContent {
      ComposeTestTheme {
        ProvideWindowInsets {
          CoilCustomImageLoader {
            Surface(color = MaterialTheme.colors.background) {
              NavigationScreen(navigator = navigator, billingHelper = billingHelper)
            }
          }
        }
      }
    }

    vpnHelper.initializeAndObserve()
    billingHelper.init()
    updateHelper.init()
    NetworkMonitor.init(applicationContext)
  }

  override fun onResume() {
    super.onResume()
    NetworkMonitor.forceUpdate()
  }
}