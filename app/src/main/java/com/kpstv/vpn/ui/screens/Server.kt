package com.kpstv.vpn.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import com.kpstv.vpn.R
import com.kpstv.vpn.data.db.repository.VpnLoadState
import com.kpstv.vpn.data.models.VpnConfiguration
import com.kpstv.vpn.extensions.utils.AppUtils.launchUrlInApp
import com.kpstv.vpn.extensions.utils.FlagUtils
import com.kpstv.vpn.ui.components.*
import com.kpstv.vpn.ui.dialogs.EmptyVpnDialog
import com.kpstv.vpn.ui.helpers.Settings
import com.kpstv.vpn.ui.helpers.VpnConfig
import com.kpstv.vpn.ui.sheets.ProtocolConnectionType
import com.kpstv.vpn.ui.sheets.ProtocolSheet
import com.kpstv.vpn.ui.theme.CommonPreviewTheme
import com.kpstv.vpn.ui.theme.dotColor
import com.kpstv.vpn.ui.theme.goldenYellow
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ServerScreen(
  vpnState: VpnLoadState,
  onBackButton: () -> Unit = {},
  onRefresh: () -> Unit = {},
  onImportButton: () -> Unit = {},
  onPremiumClick: () -> Unit = {},
  isPremiumUnlocked: Boolean = false,
  onItemClick: (VpnConfiguration, VpnConfig.ConnectionType) -> Unit,
) {
  val swipeRefreshState = rememberSwipeRefreshState(vpnState is VpnLoadState.Loading)

  val protocolBottomSheetState = rememberBottomSheetState()

  val vpnConfig = rememberSaveable { mutableStateOf(VpnConfiguration.createEmpty()) }

  val filterServer by Settings.getFilterServer()

  SwipeRefresh(
    modifier = Modifier.fillMaxSize(),
    state = swipeRefreshState,
    onRefresh = onRefresh,
    swipeEnabled = (vpnState is VpnLoadState.Completed),
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
    val freeServerIndex = vpnState.configs.indexOfFirst { !it.premium }

    val isPremiumServerExpanded = filterServer != Settings.ServerFilter.Free
    val isFreeServerExpanded = filterServer != Settings.ServerFilter.Premium

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {

      LazyColumn(
        modifier = Modifier
          .padding(horizontal = 20.dp)
      ) {
        itemsIndexed(vpnState.configs) { index, item ->
          if (index == 0) {
            key(index) {
              Spacer(
                modifier = Modifier
                  .statusBarsPadding()
                  .height(80.dp)
              )
              ServerQuickTip()
              ServerHeader(
                title = stringResource(R.string.premium_server),
                premium = true,
                expanded = isPremiumServerExpanded,
                changeToExpandedState = { Settings.setFilterServer(Settings.ServerFilter.All) }
              )
              Spacer(modifier = Modifier.height(15.dp))
            }
          }
          if (index == freeServerIndex) {
            key(freeServerIndex) {
              Spacer(modifier = Modifier.height(15.dp))
              ServerHeader(
                title = stringResource(R.string.free_server),
                expanded = isFreeServerExpanded,
                changeToExpandedState = { Settings.setFilterServer(Settings.ServerFilter.All) }
              )
              Spacer(modifier = Modifier.height(10.dp))
            }
          }

          if ((isPremiumServerExpanded && item.premium) || (isFreeServerExpanded && !item.premium)) {
            key(item.ip + index) {
              CommonItem(
                config = item,
                isPremiumUnlocked = isPremiumUnlocked,
                onPremiumClick = onPremiumClick,
                onClick = { config ->
                  vpnConfig.value = config
                  protocolBottomSheetState.show()
                }
              )
            }
          }

          if (index == vpnState.configs.size - 1) {
            key(index) {
              Spacer(
                modifier = Modifier
                  .navigationBarsPadding()
                  .height(80.dp)
              )
            }
          }
        }
      }

      Header(
        title = stringResource(R.string.choose_server),
        onBackButton = onBackButton,
        actionRow = {
          HeaderDropdownMenu()
        }
      )

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

  EmptyVpnDialog(show = vpnState is VpnLoadState.Empty)
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
      AppDropdownCheckBoxItem(
        text = stringResource(R.string.server_filter_all),
        checked = filterServerState.value == Settings.ServerFilter.All,
        onClick = {
          Settings.setFilterServer(Settings.ServerFilter.All)
          dismiss()
        }
      )
      AppDropdownCheckBoxItem(
        text = stringResource(R.string.server_filter_premium),
        checked = filterServerState.value == Settings.ServerFilter.Premium,
        onClick = {
          Settings.setFilterServer(Settings.ServerFilter.Premium)
          dismiss()
        }
      )
      AppDropdownCheckBoxItem(
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
private fun ServerHeader(
  title: String,
  premium: Boolean = false,
  expanded: Boolean = true,
  changeToExpandedState: () -> Unit = {}
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(enabled = !expanded, onClick = changeToExpandedState)
  ) {
    if (!expanded) {
      Icon(
        painter = painterResource(R.drawable.ic_baseline_play_arrow_24),
        modifier = Modifier.align(Alignment.CenterVertically),
        contentDescription = null
      )
    }
    Spacer(modifier = Modifier.width(7.dp))
    Text(
      text = title,
      style = MaterialTheme.typography.h4.copy(fontSize = 20.sp),
      color = MaterialTheme.colors.onSecondary
    )
    if (premium) {
      Spacer(modifier = Modifier.width(7.dp))
      Image(
        painter = painterResource(R.drawable.ic_crown),
        modifier = Modifier.align(Alignment.CenterVertically),
        contentDescription = "Premium"
      )
    }
  }
}

@Composable
private fun CommonItem(
  config: VpnConfiguration,
  isPremiumUnlocked: Boolean,
  onPremiumClick: () -> Unit = {},
  onClick: (VpnConfiguration) -> Unit
) {
  Spacer(modifier = Modifier.height(5.dp))

  Row(
    modifier = Modifier
      .clip(RoundedCornerShape(10.dp))
      .border(
        width = 1.5.dp,
        color = if (config.premium) goldenYellow else dotColor.copy(alpha = 0.7f),
        shape = RoundedCornerShape(10.dp)
      )
//      .wrapContentHeight()
//      .height(65.dp)
      .clickable(
        onClick = {
          if (config.premium && !isPremiumUnlocked) {
            onPremiumClick()
          } else {
            onClick.invoke(config)
          }
        },
      )
      .fillMaxWidth()
      .padding(7.dp)
  ) {
    Image(
      painter = rememberImagePainter(
        FlagUtils.getOrNull(config.country) ?: "",
        builder = {
          placeholder(R.drawable.unknown)
          crossfade(true)
        }
      ),
      modifier = Modifier
        .padding(5.dp)
        .size(40.dp)
        .align(Alignment.CenterVertically)
//        .height(40.dp)
        /* .requiredWidthIn(max = 40.dp)
         .fillMaxHeight()*/
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
      Row(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = config.country,
          style = MaterialTheme.typography.h4.copy(fontSize = 20.sp),
          overflow = TextOverflow.Ellipsis,
          maxLines = 1
        )
        Text(
          text = stringResource(R.string.server_ip, config.ip),
          modifier = Modifier
            .padding(start = 7.dp)
            .weight(1f)
            .align(Alignment.CenterVertically),
          style = MaterialTheme.typography.h5.copy(fontSize = 15.sp),
          color = MaterialTheme.colors.onSecondary,
          overflow = TextOverflow.Ellipsis,
          maxLines = 1
        )
      }
      Spacer(modifier = Modifier.height(1.dp))
      AutoSizeSingleLineText(
        text = getCommonItemSubtext(config),
        modifier = Modifier.padding(end = 5.dp),
        style = MaterialTheme.typography.subtitle2,
        color = MaterialTheme.colors.onSurface,
      )
    }
  }

  Spacer(modifier = Modifier.height(5.dp))
}

@Composable
private fun getCommonItemSubtext(config: VpnConfiguration): String {
  return if (config.sessions.isEmpty() && config.upTime.isEmpty() && config.speed.isEmpty()) {
    stringResource(R.string.server_subtitle2)
  } else {
    stringResource(R.string.server_subtitle, config.sessions, config.upTime, config.speed)
  }
}

@Composable
private fun ServerQuickTip() {
  val context = LocalContext.current
  val showTip by Settings.ServerQuickTipShown.getAsState(defaultValue = !LocalInspectionMode.current)
  QuickTip(
    message = stringResource(R.string.server_tip_text),
    visible = !showTip,
    buttonText = stringResource(R.string.learn_more),
    buttonOnClick = {
      Settings.ServerQuickTipShown.set(true)
      context.launchUrlInApp(context.getString(R.string.app_faq_server))
    }
  )
  if (!showTip) {
    Spacer(modifier = Modifier.height(15.dp))
  }
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
      config = createTestConfiguration(),
      isPremiumUnlocked = true,
      onClick = {}
    )
  }
}

@Preview
@Composable
fun PreviewCommonItemPremium() {
  CommonPreviewTheme {
    CommonItem(
      config = createTestConfiguration().copy(premium = true),
      isPremiumUnlocked = false,
      onClick = {}
    )
  }
}

@Preview
@Composable
fun PreviewServerHeaders() {
  CommonPreviewTheme {
    Column(modifier = Modifier.padding(20.dp)) {
      ServerHeader(
        title = "Premium Servers", premium = true
      )
      Spacer(modifier = Modifier.height(10.dp))
      ServerHeader(
        title = "Free Servers"
      )
      Spacer(modifier = Modifier.height(10.dp))
      ServerHeader(
        title = "Hidden Servers", expanded = false
      )
    }
  }
}

@Preview
@Composable
fun PreviewServerQuickTip() {
  CommonPreviewTheme {
    ServerQuickTip()
  }
}

private fun createTestConfiguration() =
  VpnConfiguration.createEmpty().copy(
    country = "United States",
    countryFlagUrl = "",
    ip = "192.168.1.1",
    sessions = "61 sessions",
    upTime = "89 days",
    speed = "73.24"
  )