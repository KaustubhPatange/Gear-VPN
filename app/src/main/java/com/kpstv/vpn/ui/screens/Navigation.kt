package com.kpstv.vpn.ui.screens

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kpstv.vpn.data.db.repository.VpnLoadState
import com.kpstv.vpn.data.models.asVpnConfiguration
import com.kpstv.vpn.extensions.SlideTop
import com.kpstv.vpn.ui.viewmodels.VpnViewModel
import com.kpstv.navigation.compose.ComposeNavigator
import com.kpstv.navigation.compose.Fade
import com.kpstv.navigation.compose.Route
import com.kpstv.navigation.compose.SlideRight
import com.kpstv.vpn.extensions.asVpnConfig
import com.kpstv.vpn.ui.components.*
import com.kpstv.vpn.ui.dialogs.WelcomeDialogScreen
import com.kpstv.vpn.ui.helpers.BillingHelper
import com.kpstv.vpn.ui.sheets.PremiumBottomSheet
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

  companion object {
    val key = NavigationRoute::class
  }
}

private class Load(val refresh: Boolean = false)

@Composable
fun NavigationScreen(
  navigator: ComposeNavigator,
  billingHelper: BillingHelper,
  viewModel: VpnViewModel = viewModel()
) {
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

  navigator.Setup(key = NavigationRoute.key, initial = NavigationRoute.Main()) { controller, dest ->
    when (dest) {
      // Main screen
      is NavigationRoute.Main -> MainScreen(
        publicIp = location.value?.query,
        configuration = currentConfig.value,
        connectivityStatus = connectivityStatus.value,
        onToChangeServer = {
          controller.navigateTo(NavigationRoute.Server()) {
            withAnimation {
              target = SlideRight
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
        onBackButton = { controller.goBack() },
        onRefresh = {
          vpnCollectJob.cancel()
          shouldRefresh.value = Load(refresh = true)
        },
        onImportButton = {
          controller.navigateTo(NavigationRoute.Import()) {
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
          controller.goBack()
        }
      )
      // Import screen
      is NavigationRoute.Import -> ImportScreen(
        onItemClick = { config ->
          viewModel.changeServer(config.asVpnConfiguration())

          controller.navigateTo(NavigationRoute.Main()) {
            popUpTo(NavigationRoute.Main())
            withAnimation {
              target = Fade
              current = Fade
            }
          }
        },
        isPremiumUnlocked = isPremiumUnlocked.value,
        onPremiumClick = onPremiumClick,
        goBack = { controller.goBack() }
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
