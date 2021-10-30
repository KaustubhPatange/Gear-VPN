package com.kpstv.vpn.ui.dialogs

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberImagePainter
import com.kpstv.navigation.compose.*
import com.kpstv.vpn.R
import com.kpstv.vpn.ui.components.HeaderButton
import com.kpstv.vpn.ui.components.ThemeButton
import com.kpstv.vpn.ui.helpers.Settings
import com.kpstv.vpn.ui.screens.NavigationRoute
import com.kpstv.vpn.ui.theme.CommonPreviewTheme
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.parcelize.Parcelize

@Composable
fun WelcomeDialogScreen() {
  if (LocalInspectionMode.current) return
  WelcomeDialog()
}

@OptIn(ExperimentalComposeUiApi::class, androidx.compose.animation.ExperimentalAnimationApi::class)
@Composable
private fun WelcomeDialog() {
  val navController = findNavController(key = NavigationRoute.key)

  navController.CreateDialog(
    key = WelcomeDialogRoute::class,
    dialogProperties = DialogProperties(
      dismissOnClickOutside = false,
      usePlatformDefaultWidth = false
    )
  ) {
    val dialogRouteController = rememberNavController<WelcomeScreenRoute>()

    val currentRoute by dialogRouteController.getCurrentRouteAsFlow()
      .collectAsState(initial = WelcomeScreenRoute.Welcome)

    val showBackButton = remember(currentRoute) { dialogRouteController.canGoBack() }

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(40.dp),
      verticalArrangement = Arrangement.Center
    ) {

      Column(
        modifier = Modifier
          .wrapContentHeight()
          .clip(RoundedCornerShape(5.dp))
          .background(MaterialTheme.colors.background)
          .animateContentSize()
      ) {
        dialogNavigator.Setup(
          modifier = Modifier.clipToBounds(),
          key = WelcomeScreenRoute.key,
          initial = WelcomeScreenRoute.Welcome,
          controller = dialogRouteController
        ) { dest ->
          when (dest) {
            is WelcomeScreenRoute.Welcome -> WelcomeScreen()
            is WelcomeScreenRoute.HowTo -> HowToScreen()
            is WelcomeScreenRoute.FeatureSplitTunnel -> SplitTunnelScreen()
            is WelcomeScreenRoute.FeatureGearConnect -> GearConnectScreen()
          }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Divider()
        Spacer(modifier = Modifier.height(15.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
          AnimatedVisibility(visible = showBackButton) {
            Row {
              Spacer(modifier = Modifier.width(15.dp))
              HeaderButton(
                icon = R.drawable.ic_baseline_arrow_back_24,
                iconTint = MaterialTheme.colors.secondary,
                contentDescription = "back-button",
                onClick = { goBack() }
              )
            }
          }
          Spacer(modifier = Modifier.weight(1f))
          ThemeButton(
            modifier = Modifier.animateContentSize(),
            onClick = {
              val next = currentRoute.next()
              if (next != null) {
                dialogRouteController.navigateTo(next) {
                  withAnimation {
                    target = SlideRight
                    current = SlideLeft
                  }
                }
              } else {
                dismiss()
              }
            },
            text = if (currentRoute.isLast())
              stringResource(R.string.finish)
            else stringResource(
              R.string.next,
              currentRoute.index + 1,
              WelcomeScreenRoute.totalScreens - 1
            )
          )
          Spacer(modifier = Modifier.width(15.dp))
        }
        Spacer(modifier = Modifier.height(15.dp))
      }
    }
  }

  LaunchedEffect(Unit) {
    val dialogShown = WelcomeDialogRoute.getWelcomeScreenState().firstOrNull()
    if (dialogShown == false) {
      navController.showDialog(WelcomeDialogRoute)
      WelcomeDialogRoute.setWelcomeScreenState(true)
    }
  }
}

@Composable
private fun WelcomeScreen() {
  Text(
    modifier = Modifier.padding(20.dp),
    style = MaterialTheme.typography.h5,
    text = buildAnnotatedString {
      withStyle(style = SpanStyle(fontSize = 22.sp)) {
        append(stringResource(R.string.welcome_welcome))
      }
      append("\n\n")
      withStyle(style = SpanStyle(fontSize = 16.sp, color = MaterialTheme.colors.secondary)) {
        append(stringResource(R.string.welcome_text))
      }
    })
}

@Composable
private fun HowToScreen() {
  FeatureScreen(
    name = stringResource(R.string.how_to_title),
    gif = R.drawable.how_to,
    description = stringResource(R.string.how_to_desc)
  )
}

@Composable
private fun SplitTunnelScreen() {
  FeatureScreen(
    name = stringResource(R.string.feature_title, "Split tunnel"),
    gif = R.drawable.feature_split_tunnel,
    description = stringResource(R.string.feature_split_text)
  )
}

@Composable
private fun GearConnectScreen() {
  FeatureScreen(
    name = stringResource(R.string.feature_title, "Gear Connect"),
    gif = R.drawable.feature_gear_connect,
    description = stringResource(R.string.feature_gear_connect)
  )
}

@Composable
private fun FeatureScreen(name: String, @DrawableRes gif: Int, description: String) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(20.dp)
  ) {
    Text(
      text = name,
      style = MaterialTheme.typography.h5.copy(fontSize = 22.sp)
    )
    Spacer(modifier = Modifier.height(30.dp))
    Image(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(10.dp))
        .height(280.dp)
        .border(
          width = 1.dp,
          color = MaterialTheme.colors.secondary,
          shape = RoundedCornerShape(10.dp)
        ),
      contentScale = ContentScale.Crop,
      painter = rememberImagePainter(gif),
      contentDescription = "feature-demo"
    )
    Spacer(modifier = Modifier.height(30.dp))
    Text(
      text = description,
      color = MaterialTheme.colors.secondary,
      style = MaterialTheme.typography.h5
    )
  }
}

@Parcelize
private object WelcomeDialogRoute : DialogRoute {
  private const val version = 1 // update this to show dialog for newer versions of app.

  fun getWelcomeScreenState() = Settings.getIfVersionUpdateChecked(version)
  fun setWelcomeScreenState(value: Boolean) = Settings.setIfVersionUpdateChecked(version, value)
}

private sealed class WelcomeScreenRoute(val index: Int) : DialogRoute {
  @Parcelize
  object Welcome : WelcomeScreenRoute(0)

  @Parcelize
  object HowTo : WelcomeScreenRoute(1)

  @Parcelize
  object FeatureSplitTunnel : WelcomeScreenRoute(2)

  @Parcelize
  object FeatureGearConnect : WelcomeScreenRoute(3)

  companion object Key : Route.Key<WelcomeScreenRoute> {
    const val totalScreens = 4
  }
}

private fun WelcomeScreenRoute.next(): WelcomeScreenRoute? {
  return when (this) {
    WelcomeScreenRoute.Welcome -> WelcomeScreenRoute.HowTo
    WelcomeScreenRoute.HowTo -> WelcomeScreenRoute.FeatureSplitTunnel
    WelcomeScreenRoute.FeatureSplitTunnel -> WelcomeScreenRoute.FeatureGearConnect
    WelcomeScreenRoute.FeatureGearConnect -> null
  }
}

private fun WelcomeScreenRoute.isLast() = this is WelcomeScreenRoute.FeatureGearConnect

@Preview
@Composable
fun PreviewWelcomeScreen() {
  CommonPreviewTheme {
    WelcomeScreen()
  }
}

@Preview
@Composable
fun PreviewGearConnectScreen() {
  CommonPreviewTheme {
    GearConnectScreen()
  }
}
