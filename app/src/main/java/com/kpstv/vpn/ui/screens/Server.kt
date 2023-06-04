package com.kpstv.vpn.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.kpstv.navigation.compose.DialogRoute
import com.kpstv.navigation.compose.findNavController
import com.kpstv.vpn.R
import com.kpstv.vpn.data.db.repository.VpnLoadState
import com.kpstv.vpn.data.models.VpnConfiguration
import com.kpstv.vpn.extensions.fastForEach
import com.kpstv.vpn.extensions.utils.AppUtils.launchUrlInApp
import com.kpstv.vpn.extensions.utils.VpnUtils
import com.kpstv.vpn.ui.components.*
import com.kpstv.vpn.ui.dialogs.GearAlertDialog
import com.kpstv.vpn.ui.dialogs.HowToRefreshDialog
import com.kpstv.vpn.ui.dialogs.RefreshDialog
import com.kpstv.vpn.ui.helpers.Settings
import com.kpstv.vpn.ui.helpers.VpnConfig
import com.kpstv.vpn.ui.sheets.ProtocolConnectionType
import com.kpstv.vpn.ui.sheets.ProtocolSheet
import com.kpstv.vpn.ui.theme.*
import com.kpstv.vpn.ui.viewmodels.FlagViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.parcelize.Parcelize
import okhttp3.internal.immutableListOf

@Composable
fun ServerScreen(
  vpnState: VpnLoadState,
  onBackButton: () -> Unit = {},
  onForceRefresh: () -> Unit = {},
  onImportButton: () -> Unit = {},
  onPremiumClick: () -> Unit = {},
  isPremiumUnlocked: Boolean = false,
  flagViewModel: FlagViewModel = composeViewModel(),
  onItemClick: (VpnConfiguration, VpnConfig.ConnectionType) -> Unit,
) {
  val swipeRefreshState = rememberSwipeRefreshState(vpnState is VpnLoadState.Loading)

  val protocolBottomSheetState = rememberBottomSheetState()

  val vpnConfig = rememberSaveable { mutableStateOf(VpnConfiguration.createEmpty()) }
  val selectedVpnCountry = rememberSaveable { mutableStateOf("") }

  val configs by remember(vpnState) {
    derivedStateOf { vpnState.configs.groupBy { it.country }.mapValues { it.value.toImmutableList() } }
  }
  val countries by remember(vpnState) {
    derivedStateOf {
      vpnState.configs.map { it.country }.distinct()
    }
  }

  val navController = findNavController(NavigationRoute.key)

  android.util.Log.e("ServerScreen", "Recomposed")

  SwipeRefresh(
    modifier = Modifier.fillMaxSize(),
    state = swipeRefreshState,
    onRefresh = { navController.showDialog(ServerRefreshDialog) },
    swipeEnabled = (vpnState is VpnLoadState.Completed || vpnState.isError()),
    indicator = { state, trigger ->
      SwipeRefreshIndicator(
        state = state,
        refreshTriggerDistance = trigger + 20.dp,
        backgroundColor = MaterialTheme.colors.primary,
        contentColor = MaterialTheme.colors.onSecondary,
        refreshingOffset = 80.dp
      )
    }
  ) {

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {

      LazyColumn(
        modifier = Modifier
          .padding(horizontal = 20.dp)
      ) {
        itemsIndexed(countries) { index, item ->
          if (index == 0) {
            key("header$index") {
              Spacer(
                modifier = Modifier
                  .statusBarsPadding()
                  .height(80.dp)
              )
              RefreshInterruptBanner(
                visible = vpnState is VpnLoadState.Interrupt,
                onRefresh = onForceRefresh,
              )
              ScreenQuickTips()
            }
          }

          key(item) {
            CommonItem(
              configs = configs[item]!!,
              isPremiumUnlocked = isPremiumUnlocked,
              getFlagUrl = flagViewModel::getFlagUrlByCountry,
              onClick = { config ->
                vpnConfig.value = config
                protocolBottomSheetState.show()
              },
              isExpanded = selectedVpnCountry.value == item,
              onPremiumClick = onPremiumClick,
              onExpandedClick = {
                if (selectedVpnCountry.value == item) {
                  selectedVpnCountry.value = ""
                } else {
                  selectedVpnCountry.value = item
                }
              }
            )
          }

          if (index == configs.size - 1) {
            key("endSpacer$index") {
              Spacer(
                modifier = Modifier
                  .navigationBarsPadding()
                  .height(80.dp)
              )
            }
          }
        }
      }

      AnimatedVisibility(
        visible = !vpnState.isError(),
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically()
      ) {
        Header(
          title = stringResource(R.string.choose_server),
          onBackButton = onBackButton,
          actionRow = {
            HeaderDropdownMenu()
          }
        )
      }

      Footer(
        modifier = Modifier.align(Alignment.BottomCenter),
        onImportButton = onImportButton
      )
    }
  }

  ProtocolSheet(
    protocolSheetState = protocolBottomSheetState,
    vpnConfig = vpnConfig.value,
    onItemClick = { type ->
      when (type) {
        ProtocolConnectionType.TCP -> onItemClick.invoke(
          vpnConfig.value,
          VpnConfig.ConnectionType.TCP
        )

        ProtocolConnectionType.UDP -> onItemClick.invoke(
          vpnConfig.value,
          VpnConfig.ConnectionType.UDP
        )
      }
    }
  )

  AnimatedVisibility(visible = vpnState.isError(), enter = fadeIn(), exit = fadeOut()) {
    Column {
      Spacer(modifier = Modifier.height(20.dp))
      ErrorVpnScreen(
        modifier = Modifier.padding(20.dp),
        title = stringResource(R.string.err_something_went_wrong),
        onDismiss = onBackButton,
        onRefresh = onForceRefresh
      )
    }
  }

  // How to refresh VPN servers Dialog
  HowToRefreshDialog()

  // Server confirm refresh dialog
  ServerRefreshConfirmDialog(onRefresh = onForceRefresh)
}

@Composable
private fun HeaderDropdownMenu(expanded: Boolean = false) {
  val expandedState = remember { mutableStateOf(expanded) }

  val filterServerState = Settings.getFilterServer()

  val dismiss = remember { { expandedState.value = false } }

  HeaderButton(
    icon = R.drawable.ic_baseline_filter_list_24,
    contentDescription = "filter server",
    tooltip = stringResource(R.string.server_filter),
    onClick = { expandedState.value = true }
  )
  AppDropdownMenu(
    title = stringResource(R.string.filter_server),
    expandedState = expandedState,
    content = {
      AppDropdownRadioButtonItem(
        text = stringResource(R.string.server_filter_all),
        checked = filterServerState.value == Settings.ServerFilter.All,
        onClick = {
          Settings.setFilterServer(Settings.ServerFilter.All)
          dismiss()
        }
      )
      AppDropdownRadioButtonItem(
        text = stringResource(R.string.server_filter_premium),
        checked = filterServerState.value == Settings.ServerFilter.Premium,
        onClick = {
          Settings.setFilterServer(Settings.ServerFilter.Premium)
          dismiss()
        }
      )
      AppDropdownRadioButtonItem(
        text = stringResource(R.string.server_filter_free),
        checked = filterServerState.value == Settings.ServerFilter.Free,
        onClick = {
          Settings.setFilterServer(Settings.ServerFilter.Free)
          dismiss()
        }
      )
    }
  )
}

@Composable
private fun Footer(modifier: Modifier = Modifier, onImportButton: () -> Unit) {
  Column(
    modifier = modifier.then(
      Modifier
        .background(color = MaterialTheme.colors.background.copy(alpha = 0.93f))
        .navigationBarsPadding()
    )
  ) {
    Divider(color = MaterialTheme.colors.primaryVariant)

    Spacer(modifier = Modifier.height(10.dp))

    ThemeButton(
      onClick = onImportButton,
      modifier = Modifier
        .padding(horizontal = 20.dp)
        .height(55.dp)
        .clip(RoundedCornerShape(10.dp))
        .align(Alignment.CenterHorizontally),
      text = stringResource(R.string.import_open_vpn)
    )

    Spacer(modifier = Modifier.height(10.dp))
  }
}


@Composable
private fun CommonItem(
  configs: ImmutableList<VpnConfiguration>,
  isPremiumUnlocked: Boolean,
  getFlagUrl: (String) -> Flow<String>,
  onPremiumClick: () -> Unit,
  onClick: (VpnConfiguration) -> Unit,
  isExpanded: Boolean,
  onExpandedClick: () -> Unit
) {
  Spacer(modifier = Modifier.height(5.dp))

  android.util.Log.e("CommonItem", "Recomposed: ${configs[0].country}")

  Column(
    modifier = Modifier
      .clip(RoundedCornerShape(10.dp))
      .border(
        width = 1.5.dp,
        color = dotColor.copy(alpha = 0.7f),
        shape = RoundedCornerShape(10.dp)
      )
      .fillMaxWidth()
      .animateContentSize(
        animationSpec = tween(durationMillis = 150)
      )
  ) {
    Row(
      modifier = Modifier
        .clickable(
          onClick = onExpandedClick,
        )
        .padding(7.dp)
    ) {
      val flagUrl by getFlagUrl(configs[0].country).collectAsState(initial = "")
//      Column(
//        modifier = Modifier.padding(5.dp)
//          .size(40.dp)
//      ) {}
      Image(
        painter = rememberImagePainter(
          data = flagUrl,
          builder = {
            placeholder(R.drawable.unknown)
            crossfade(true)
          }
        ),
        modifier = Modifier
          .padding(5.dp)
          .size(40.dp)
          .align(Alignment.CenterVertically)
          .scale(1f),
        contentDescription = "Country flag",
        contentScale = ContentScale.Fit
      )

      Spacer(modifier = Modifier.width(10.dp))

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .align(Alignment.CenterVertically)
      ) {
        Text(
          text = configs[0].country,
          style = MaterialTheme.typography.h4.copy(fontSize = 20.sp),
          overflow = TextOverflow.Ellipsis,
          maxLines = 1
        )
        Text(
          text = stringResource(R.string.server_location, configs.size),
          style = MaterialTheme.typography.h5.copy(fontSize = 15.sp),
          color = MaterialTheme.colors.onSecondary,
        )
      }
    }

    Spacer(modifier = Modifier.height(3.dp))

    if (isExpanded) {
      Column(modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 7.dp)) {
        Spacer(
          modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(horizontal = 10.dp)
            .background(dotColor)
        )
        Spacer(modifier = Modifier.height(10.dp))
        configs.fastForEach { config ->
          key(config.ip) {
            CommonChildItem(
              config = config,
              onClick = onClick,
              isPremiumUnlocked = isPremiumUnlocked,
              onPremiumClick = onPremiumClick
            )
            Spacer(modifier = Modifier.height(2.dp))
          }
        }
      }
    }
  }

  Spacer(modifier = Modifier.height(5.dp))
}

@Composable
private fun CommonChildItem(
  config: VpnConfiguration,
  onClick: (VpnConfiguration) -> Unit,
  isPremiumUnlocked: Boolean = false,
  onPremiumClick: () -> Unit = {},
) {

  Row(
    modifier = Modifier
      .clip(RoundedCornerShape(5.dp))
      .fillMaxWidth()
      .clickable(
        onClick = {
          if (config.premium && !isPremiumUnlocked) {
            onPremiumClick()
          } else {
            onClick.invoke(config)
          }
        }
      )
      .padding(vertical = 5.dp, horizontal = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Column {
      Text(
        text = config.ip,
        style = MaterialTheme.typography.h5.copy(fontSize = 15.sp),
        color = MaterialTheme.colors.onSecondary,
      )
      Spacer(modifier = Modifier.height(1.dp))
      AutoSizeSingleLineText(
        text = getCommonItemSubtext(config),
        modifier = Modifier.padding(end = 5.dp),
        style = MaterialTheme.typography.subtitle2,
        color = MaterialTheme.colors.onSurface,
      )
    }
    if (config.premium) {
      Image(
        modifier = Modifier.size(18.dp),
        painter = painterResource(R.drawable.ic_crown),
        contentDescription = "Get premium"
      )
    }
  }
}

@Composable
private fun getCommonItemSubtext(config: VpnConfiguration): String {
  return if (config.sessions.isEmpty() && config.upTime.isEmpty() && config.speed == 0f) {
    stringResource(R.string.server_subtitle2)
  } else if (config.sessions.isEmpty() && config.upTime.isNotEmpty() && config.speed == 0f) {
    config.upTime
  } else if (config.sessions.isEmpty() && config.upTime.isNotEmpty() && config.speed != 0f) {
    stringResource(
      R.string.server_subtitle3,
      config.upTime,
      VpnUtils.formatVpnGateSpeed(config.speed)
    )
  } else {
    stringResource(
      R.string.server_subtitle,
      config.sessions,
      config.upTime,
      VpnUtils.formatVpnGateSpeed(config.speed)
    )
  }
}

@Composable
fun RefreshInterruptBanner(visible: Boolean, onRefresh: () -> Unit) {
  QuickTip(
    message = stringResource(R.string.error_vpn_refresh),
    visible = visible,
    cardColor = MaterialTheme.colors.error,
    textColor = Color.White,
    button = {
      Button(
        onClick = onRefresh,
        colors = ButtonDefaults.buttonColors(backgroundColor = GearVPNTheme.colors.errorButton)
      ) {
        Text(text = stringResource(R.string.error_btn_refresh), color = Color.White)
      }
    }
  )
  if (visible) {
    Spacer(modifier = Modifier.height(15.dp))
  }
}

@Composable
private fun ScreenQuickTips() {
  ServerQuickTip()
  HowToRefreshQuickTip()
}

@Composable
private fun ServerQuickTip() {
  val context = LocalContext.current
  val showTip by Settings.ServerQuickTipShown.getAsState(defaultValue = !LocalInspectionMode.current)
  QuickTip(
    message = stringResource(R.string.server_tip_text),
    visible = !showTip,
    button = {
      ThemeButton(
        onClick = {
          Settings.ServerQuickTipShown.set(true)
          context.launchUrlInApp(context.getString(R.string.app_faq_server))
        },
        text = stringResource(R.string.learn_more)
      )
    }
  )
  if (!showTip) {
    Spacer(modifier = Modifier.height(15.dp))
  }
}

@Composable
private fun HowToRefreshQuickTip() {
  val navController = findNavController(key = NavigationRoute.key)
  val serverTipShown by Settings.ServerQuickTipShown.getAsState()
  val showTip by Settings.HowToRefreshTipShown.getAsState(defaultValue = !LocalInspectionMode.current)
  QuickTip(
    message = stringResource(R.string.how_to_refresh_tip_text),
    visible = !showTip && serverTipShown,
    button = {
      ThemeButton(
        onClick = {
          Settings.HowToRefreshTipShown.set(true)
          navController.showDialog(RefreshDialog)
        },
        text = stringResource(R.string.learn_more)
      )
    }
  )
  if (!showTip) {
    Spacer(modifier = Modifier.height(15.dp))
  }
}

@Parcelize
private object ServerRefreshDialog : DialogRoute

@Composable
private fun ServerRefreshConfirmDialog(onRefresh: () -> Unit) {
  GearAlertDialog(
    route = ServerRefreshDialog::class,
    title = stringResource(R.string.server_refresh_dialog_title),
    message = stringResource(R.string.server_refresh_dialog_message),
    onPositiveClick = onRefresh,
    showNegativeButton = true,
  )
}

@Preview
@Composable
fun PreviewFooter() {
  CommonPreviewTheme {
    Footer {}
  }
}

@Preview
@Composable
fun PreviewCommonItem() {
  CommonPreviewTheme {
    CommonItem(
      configs = listOf(createTestConfiguration(), createTestConfiguration()).toImmutableList(),
      getFlagUrl = { flowOf("") },
      isPremiumUnlocked = true,
      onClick = {},
      isExpanded = true,
      onExpandedClick = {},
      onPremiumClick = {}
    )
  }
}

@Preview
@Composable
fun PreviewCommonChildItem() {
  CommonPreviewTheme {
    CommonChildItem(config = createTestConfiguration(), onClick = {})
  }
}

@Preview
@Composable
fun PreviewServerQuickTip() {
  CommonPreviewTheme {
    ServerQuickTip()
  }
}

@Preview
@Composable
fun PreviewRefreshInterruptBanner() {
  CommonPreviewTheme {
    RefreshInterruptBanner(
      visible = true,
      onRefresh = {}
    )
  }
}

private fun createTestConfiguration() =
  VpnConfiguration.createEmpty().copy(
    country = "United States",
    countryFlagUrl = "",
    ip = "192.168.1.1",
    sessions = "61 sessions",
    upTime = "89 days",
    speed = 73.24f,
    premium = true,
  )