package com.kpstv.vpn.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsHeight
import com.google.accompanist.insets.statusBarsPadding
import com.kpstv.vpn.BuildConfig
import com.kpstv.vpn.R
import com.kpstv.vpn.extensions.utils.AppUtils.launchUrl
import com.kpstv.vpn.ui.components.Header
import com.kpstv.vpn.ui.components.HeaderButton
import com.kpstv.vpn.ui.theme.CommonPreviewTheme
import com.kpstv.vpn.ui.theme.dotColor
import com.kpstv.vpn.ui.theme.foregroundColor

@Composable
fun AboutScreen(goBack: () -> Unit) {
  Box(
    modifier = Modifier
      .navigationBarsPadding()
      .fillMaxSize()
  ) {

    val context = LocalContext.current

    val infiniteTransition = rememberInfiniteTransition()
    val iconAngle by infiniteTransition.animateFloat(
      initialValue = 0F,
      targetValue = -360F,
      animationSpec = infiniteRepeatable(
        animation = tween(30000, easing = LinearEasing, delayMillis = 1000)
      )
    )

    Header(
      title = stringResource(R.string.about),
      onBackButton = goBack
    )
    Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.statusBarsHeight(additional = 5.dp))
      CompositionLocalProvider(LocalContentColor provides foregroundColor) {
        Icon(
          modifier = Modifier
            .size(150.dp)
            .rotate(iconAngle),
          painter = painterResource(R.drawable.ic_logo),
          contentDescription = "logo"
        )
        Spacer(modifier = Modifier.height(30.dp))
        Text(
          text = stringResource(R.string.app_name),
          style = MaterialTheme.typography.h4.copy(fontSize = 35.sp)
        )
      }
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = "v${BuildConfig.VERSION_NAME}",
        color = dotColor,
        style = MaterialTheme.typography.button
      )
      Spacer(modifier = Modifier.height(30.dp))
      Text(
        modifier = Modifier.padding(horizontal = 25.dp),
        text = stringResource(R.string.app_description),
        color = MaterialTheme.colors.secondary,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.h5
      )
      Spacer(modifier = Modifier.height(10.dp))
      CompositionLocalProvider(LocalContentColor provides MaterialTheme.colors.secondary) {
        Row {
          HeaderButton(
            icon = R.drawable.ic_github,
            contentDescription = "github",
            tooltip = "Github",
            onClick = { context.launchUrl(context.getString(R.string.app_github)) }
          )
          Spacer(modifier = Modifier.width(5.dp))
          HeaderButton(
            icon = R.drawable.ic_privacy_policy,
            contentDescription = "privacy-policy",
            tooltip = "Privacy policy",
            onClick = { context.launchUrl(context.getString(R.string.app_privacy_policy)) }
          )
        }
      }
    }
  }
}

@Preview
@Composable
fun PreviewAboutScreen() {
  CommonPreviewTheme {
    AboutScreen(
      goBack = {}
    )
  }
}