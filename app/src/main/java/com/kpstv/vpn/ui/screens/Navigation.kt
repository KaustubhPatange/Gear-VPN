package com.kpstv.vpn.ui.screens

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kpstv.navigation.compose.*
import com.kpstv.vpn.R
import com.kpstv.vpn.data.db.repository.VpnLoadState
import com.kpstv.vpn.data.models.asVpnConfig
import com.kpstv.vpn.extensions.SlideTop
import com.kpstv.vpn.extensions.asVpnConfig
import com.kpstv.vpn.ui.components.ConnectionStatusBox
import com.kpstv.vpn.ui.components.rememberBottomSheetState
import com.kpstv.vpn.ui.dialogs.WelcomeDialogScreen
import com.kpstv.vpn.ui.helpers.BillingHelper
import com.kpstv.vpn.ui.sheets.PremiumBottomSheet
import com.kpstv.vpn.ui.viewmodels.VpnViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.parcelize.Parcelize

sealed interface NavigationRoute : Route {
  @Parcelize
  @Immutable
  data class Main(private val noArg: String = "") : NavigationRoute

  @Parcelize
  @Immutable
  data class Server(private val noArg: String = "") : NavigationRoute

  @Parcelize
  @Immutable
  data class Import(private val noArg: String = "") : NavigationRoute

  @Parcelize
  @Immutable
  data class About(private val noArg: String = "") : NavigationRoute

  companion object Key : Route.Key<NavigationRoute>
}

private class Load(val refresh: Boolean = false)

@Composable
fun NavigationScreen(
  navigator: ComposeNavigator,
  billingHelper: BillingHelper,
  viewModel: VpnViewModel = viewModel()
) {
  val context = LocalContext.current
  val shouldRefresh = remember { mutableStateOf(Load(), policy = referentialEqualityPolicy()) }
  val vpnCollectJob = remember(shouldRefresh.value) { SupervisorJob() }

  val premiumBottomSheet = rememberBottomSheetState()

  val location = viewModel.publicIp.collectAsState()
  val currentConfig = viewModel.currentVpn.collectAsState()
  val vpnLoadState = viewModel.fetchServers(shouldRefresh.value.refresh)
    .collectAsState(initial = VpnLoadState.Loading(), context = vpnCollectJob + Dispatchers.IO)

  val connectivityStatus = viewModel.connectivityStatus.collectAsState()

  val isPremiumUnlocked = billingHelper.isPurchased.collectAsState(initial = false)

  val onPurchaseComplete = billingHelper.purchaseComplete.collectAsState()
  if (onPurchaseComplete.value.sku == BillingHelper.purchase_sku) {
    premiumBottomSheet.show()
  }

  val onPurchaseClick: () -> Unit = {
    billingHelper.launch()
    premiumBottomSheet.hide()
  }

  val onPremiumClick: () -> Unit = { premiumBottomSheet.show() }

  val navController = rememberNavController<NavigationRoute>()

  navigator.Setup(
    key = NavigationRoute.key,
    initial = NavigationRoute.Main(),
    controller = navController
  ) { dest ->
    when (dest) {
      // Main screen
      is NavigationRoute.Main -> MainScreen(
        publicIp = location.value?.query,
        configuration = currentConfig.value,
        connectivityStatus = connectivityStatus.value,
        onToChangeServer = {
          navController.navigateTo(NavigationRoute.Server()) {
            withAnimation {
              target = SlideRight
              current = Fade
            }
          }
        },
        onToAboutScreen = {
          navController.navigateTo(NavigationRoute.About()) {
            withAnimation {
              target = SlideTop
              current = Fade
            }
          }
        },
        onConnectClick = {
          viewModel.connect()
        },
        onDisconnect = {
          viewModel.disconnect()
        },
        onPremiumClick = onPremiumClick,
        onDisallowedAppListChanged = {
          viewModel.reconnect()
        }
      )
      // Server screen
      is NavigationRoute.Server -> ServerScreen(
        vpnState = vpnLoadState.value,
        onBackButton = { navController.goBack() },
        onRefresh = {
          vpnCollectJob.cancel()
          shouldRefresh.value = Load(refresh = true)
        },
        onImportButton = {
          navController.navigateTo(NavigationRoute.Import()) {
            withAnimation {
              target = SlideTop
              current = Fade
            }
          }
        },
        isPremiumUnlocked = isPremiumUnlocked.value,
        onPremiumClick = onPremiumClick,
        onItemClick = { config, type ->
          viewModel.changeServer(config.asVpnConfig(type))
          navController.goBack()
        }
      )
      // Import screen
      is NavigationRoute.Import -> ImportScreen(
        onItemClick = { config ->
          viewModel.changeServer(
            config.asVpnConfig()
              .run { copy(country = context.getString(R.string.import_config_country, country)) })

          navController.navigateTo(NavigationRoute.Main()) {
            popUpTo(NavigationRoute.Main::class)
            withAnimation {
              target = Fade
              current = Fade
            }
          }
        },
        isPremiumUnlocked = isPremiumUnlocked.value,
        onPremiumClick = onPremiumClick,
        goBack = { navController.goBack() }
      )
      // About screen
      is NavigationRoute.About -> AboutScreen(
        goBack = { navController.goBack() }
      )
    }

    /* Connection Status : An overlay on status bar */
    ConnectionStatusBox()

    /* Premium Bottom Sheet */
    PremiumBottomSheet(
      premiumBottomSheet = premiumBottomSheet,
      isPremiumUnlocked = isPremiumUnlocked.value,
      onPremiumClick = onPurchaseClick
    )

    /* Welcome Dialog */
    WelcomeDialogScreen()
  }
}
