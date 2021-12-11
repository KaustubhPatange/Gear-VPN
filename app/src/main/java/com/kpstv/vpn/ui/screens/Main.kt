package com.kpstv.vpn.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
import com.kpstv.navigation.compose.findNavController
import com.kpstv.vpn.R
import com.kpstv.vpn.extensions.utils.AppUtils.launchUrl
import com.kpstv.vpn.extensions.utils.NetworkMonitor
import com.kpstv.vpn.ui.components.*
import com.kpstv.vpn.ui.dialogs.AppsDialog
import com.kpstv.vpn.ui.dialogs.AppsDialogMain
import com.kpstv.vpn.ui.helpers.VpnConfig
import com.kpstv.vpn.ui.theme.CommonPreviewTheme
import com.kpstv.vpn.ui.theme.cyanDark
import com.kpstv.vpn.ui.theme.greenColorDark
import com.kpstv.vpn.ui.theme.purpleColor
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainScreen(
  publicIp: String?,
  configuration: VpnConfig = VpnConfig.createEmpty(),
  connectivityStatus: ConnectivityStatus = ConnectivityStatus.NONE,
  getFlagUrl: (country: String) -> Flow<String>,
  onToChangeServer: () -> Unit,
  onToAboutScreen: () -> Unit,
  onConnectClick: () -> Unit,
  onDisconnect: () -> Unit,
  onPremiumClick: () -> Unit,
  onDisallowedAppListChanged: () -> Unit
) {
  val context = LocalContext.current

  val ipTextColor: Color by animateColorAsState(
    if (connectivityStatus.isConnected()) greenColorDark else MaterialTheme.colors.error
  )

  val isNetworkConnected by NetworkMonitor.connection.collectAsState()

  val ipText = if (!connectivityStatus.isConnected())
    stringResource(R.string.vpn_status, publicIp ?: stringResource(R.string.vpn_public_ip_unknown))
  else stringResource(R.string.vpn_status_hidden)

  Column(
    modifier = Modifier
      .padding(top = 5.dp)
      .statusBarsPadding()
      .navigationBarsPadding()
      .fillMaxSize()
  ) {
    Box(modifier = Modifier.fillMaxWidth()) {
      /* Settings button */
      SettingsDropdownMenu(
        modifier = Modifier
          .align(Alignment.CenterStart)
          .padding(start = 10.dp),
        goToAboutScreen = onToAboutScreen
      )

      Text(
        text = stringResource(R.string.app_name),
        modifier = Modifier
          .align(Alignment.Center)
          .padding(top = 5.dp)
          .pointerInput(Unit) {
            detectTapGestures(
              onLongPress = { context.launchUrl(context.getString(R.string.app_google_play)) }
            )
          },
        style = MaterialTheme.typography.h4
      )
      IconButton(
        onClick = onPremiumClick,
        modifier = Modifier
          .padding(end = 10.dp)
          .align(Alignment.CenterEnd)
      ) {
        Image(painter = painterResource(R.drawable.ic_crown), contentDescription = "Get premium")
      }
    }

    Spacer(modifier = Modifier.weight(0.5f))

    CircularBox(status = connectivityStatus)

    Spacer(modifier = Modifier.padding(top = 30.dp))

    Text(
      text = stringResource(R.string.vpn_public_ip),
      modifier = Modifier.align(Alignment.CenterHorizontally),
      style = MaterialTheme.typography.subtitle1,
      color = MaterialTheme.colors.onSecondary
    )

    Text(
      text = ipText,
      modifier = Modifier
        .align(Alignment.CenterHorizontally)
        .padding(top = 3.dp)
        .animateContentSize(),
      style = MaterialTheme.typography.subtitle1,
      color = ipTextColor,
    )

    Spacer(modifier = Modifier.weight(1f))

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(15.dp)
        .height(90.dp)
        .border(
          border = BorderStroke(
            width = 2.dp,
            brush = Brush.horizontalGradient(
              0.5f to cyanDark, 1f to purpleColor
            )
          ),
          shape = RoundedCornerShape(10.dp)
        )
        .padding(10.dp)
    ) {
      if (!configuration.country.startsWith("Custom")) {

        val flagUrl = getFlagUrl(configuration.country).collectAsState(initial = "")

        Image(
          painter = rememberImagePainter(flagUrl.value,
            builder = {
              placeholder(R.drawable.unknown)
              crossfade(true)
            }
          ),
          modifier = Modifier
            .padding(10.dp)
            .requiredWidthIn(max = 50.dp)
            .fillMaxHeight(),
          contentDescription = "country",
        )
      }

      Spacer(modifier = Modifier.width(10.dp))

      Column(
        modifier = Modifier
          .weight(1f)
          .align(Alignment.CenterVertically)
      ) {
        AutoSizeSingleLineText(
          text = configuration.country,
          style = MaterialTheme.typography.h2
        )
        AutoSizeSingleLineText(
          text = stringResource(
            id = R.string.main_ip,
            configuration.ip,
            configuration.connectionType.name
          ),
          style = MaterialTheme.typography.subtitle2,
          color = MaterialTheme.colors.onSecondary
        )
      }

      Spacer(modifier = Modifier.padding(start = 10.dp))

      ThemeButton(
        onClick = onToChangeServer,
        modifier = Modifier
          .fillMaxHeight()
          .clip(RoundedCornerShape(5.dp)),
        text = stringResource(R.string.change_server),
        enabled = !connectivityStatus.isConnected() && !connectivityStatus.isConnecting()
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(5.dp)
        .padding(horizontal = 32.dp)
        .height(55.dp)
    ) {
      ThemeButton(
        enabled = (
          (!connectivityStatus.isConnecting() && configuration.isNotEmpty()) // when VPN connected or disconnected
            && !(connectivityStatus.isDisconnected() && !isNetworkConnected) // when network present & VPN connected
          ),
        onClick = {
          if (connectivityStatus.isConnected()) {
            onDisconnect.invoke()
          } else {
            onConnectClick.invoke()
          }
        },
        modifier = Modifier
          .weight(1f)
          .clip(RoundedCornerShape(10.dp))
          .fillMaxHeight()
          .animateContentSize(),
        text = if (!connectivityStatus.isConnected())
          stringResource(R.string.status_connect)
        else stringResource(R.string.status_disconnect)
      )

      AnimatedVisibility(visible = connectivityStatus.isConnecting()) {
        ThemeButton(
          onClick = onDisconnect,
          modifier = Modifier
            .padding(start = 20.dp)
            .clip(RoundedCornerShape(10.dp))
            .fillMaxHeight(),
          text = stringResource(R.string.stop)
        )
      }
    }

    Spacer(modifier = Modifier.height(20.dp))
  }

  AppsDialogMain(
    onDisallowedAppListChanged = onDisallowedAppListChanged
  )
}

@Composable
private fun SettingsDropdownMenu(
  modifier: Modifier = Modifier,
  goToAboutScreen: () -> Unit
) {
  val expandedState = remember { mutableStateOf(false) }

  val dismiss = remember { { expandedState.value = false } }

  IconButton(
    modifier = modifier,
    onClick = { expandedState.value = true }
  ) {
    Icon(
      tint = MaterialTheme.colors.secondary,
      painter = painterResource(R.drawable.ic_gear_icon),
      contentDescription = stringResource(R.string.more_options)
    )
  }

  if (LocalInspectionMode.current) return // for preview

  val controller = findNavController(key = NavigationRoute.key)

  AppDropdownMenu(
    title = stringResource(R.string.more_options),
    expandedState = expandedState,
    offset = DpOffset(15.dp, 0.dp)
  ) {
    AppDropdownIconItem(
      title = stringResource(R.string.filter_apps),
      painter = painterResource(R.drawable.ic_apps),
      contentDescription = "filter apps",
      onClick = {
        controller.showDialog(AppsDialog)
        dismiss()
      }
    )
    AppDropdownIconItem(
      title = stringResource(R.string.about),
      painter = painterResource(R.drawable.ic_help),
      contentDescription = "about",
      onClick = {
        goToAboutScreen()
        dismiss()
      }
    )
  }

}

@Preview
@Composable
fun PreviewStartScreen() {
  CommonPreviewTheme {
    MainScreen(
      publicIp = "104.156.232.238",
      getFlagUrl = { MutableStateFlow("") },
      onToChangeServer = {},
      onToAboutScreen = {},
      onDisallowedAppListChanged = {},
      onPremiumClick = {},
      onConnectClick = {},
      onDisconnect = {}
    )
  }
}