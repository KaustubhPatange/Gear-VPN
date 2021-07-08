package com.kpstv.composetest.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.coil.rememberCoilPainter
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
import com.kpstv.composetest.R
import com.kpstv.composetest.data.models.VpnConfiguration
import com.kpstv.composetest.ui.components.CircularBox
import com.kpstv.composetest.ui.components.ConnectivityStatus
import com.kpstv.composetest.ui.components.ThemeButton
import com.kpstv.composetest.ui.theme.ComposeTestTheme
import com.kpstv.composetest.ui.theme.cyanDark
import com.kpstv.composetest.ui.theme.greenColorDark
import com.kpstv.composetest.ui.theme.purpleColor

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainScreen(
  publicIp: String?,
  configuration: VpnConfiguration = VpnConfiguration.createEmpty(),
  onChangeServer: () -> Unit = {}
) {
  val connectivityStatus = remember { mutableStateOf(ConnectivityStatus.NONE) }

  val ipTextColor: Color by animateColorAsState(
    if (connectivityStatus.value == ConnectivityStatus.CONNECTED) greenColorDark else MaterialTheme.colors.error
  )

  val ipText = stringResource(R.string.vpn_status, publicIp ?: stringResource(R.string.vpn_public_ip_unknown))

  Column(
    modifier = Modifier
      .padding(top = 15.dp)
      .statusBarsPadding()
      .navigationBarsPadding()
      .fillMaxSize()
  ) {
    Text(
      text = "Sparkle VPN",
      modifier = Modifier.align(Alignment.CenterHorizontally),
      style = MaterialTheme.typography.h4
    )

    Spacer(modifier = Modifier.weight(0.5f))

    CircularBox(status = connectivityStatus.value)

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
      AnimatedVisibility(visible = configuration.isNotEmpty()) {
        Image(
          painter =  rememberCoilPainter(configuration.countryFlagUrl),
          modifier = Modifier
            .padding(10.dp)
            .requiredWidthIn(max = 50.dp)
            .fillMaxHeight()
            .clip(shape = CircleShape)
            .scale(0.7f),
          contentDescription = "country",
        )
      }

      Spacer(modifier = Modifier.padding(start = 10.dp))

      Column(modifier = Modifier
        .weight(1f)
        .align(Alignment.CenterVertically)) {
        Text(
          text = configuration.country,
          style = MaterialTheme.typography.h2,
          overflow = TextOverflow.Ellipsis,
          maxLines = 1
        )
        Text(
          text = configuration.ip,
          style = MaterialTheme.typography.subtitle2,
          color = MaterialTheme.colors.onSecondary
        )
      }

      Spacer(modifier = Modifier.padding(start = 10.dp))

      ThemeButton(
        onClick = onChangeServer,
        modifier = Modifier
          .fillMaxHeight()
          .clip(RoundedCornerShape(5.dp)),
        text = stringResource(R.string.change_server)
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
        onClick = { },
        modifier = Modifier
          .weight(1f)
          .clip(RoundedCornerShape(10.dp))
          .fillMaxHeight()
          .animateContentSize(),
        text = stringResource(R.string.status_connect)
      )

      AnimatedVisibility(visible = connectivityStatus.value == ConnectivityStatus.CONNECTING) {
        Spacer(modifier = Modifier.padding(start = 20.dp))

        ThemeButton(
          onClick = {},
          modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .fillMaxHeight(),
          text = stringResource(R.string.stop)
        )
      }
    }

    Spacer(modifier = Modifier.height(20.dp))
  }
}

@Preview(showBackground = true)
@Composable
fun PreviewStartScreen() {
  ComposeTestTheme {
    Surface(color = MaterialTheme.colors.background) {
      MainScreen(publicIp = "104.156.232.238")
    }
  }
}