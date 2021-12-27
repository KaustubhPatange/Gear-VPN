package com.kpstv.vpn.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.kpstv.vpn.R
import com.kpstv.vpn.ui.theme.CommonPreviewTheme

@Composable
fun ErrorVpnScreen(modifier: Modifier = Modifier, title: String, onDismiss: () -> Unit, onRefresh: () -> Unit) {
  ErrorContent(modifier = modifier, title = title, onDismiss = onDismiss, onRefresh = onRefresh)
}

@Composable
private fun ErrorContent(modifier: Modifier = Modifier, title: String, onDismiss: () -> Unit, onRefresh: () -> Unit) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.cat_tail_wag))
    val progress by animateLottieCompositionAsState(
      composition = composition,
      iterations = LottieConstants.IterateForever
    )
    LottieAnimation(
      modifier = Modifier.size(250.dp), // it needs fixed size otherwise it fills the entire screen, [cc](github.com/airbnb/lottie-android/issues/1866)
      composition = composition,
      progress = progress
    )
    Spacer(modifier = Modifier.height(20.dp))
    Text(
      text = title,
      style = MaterialTheme.typography.h5.copy(fontSize = 30.sp),
      textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(20.dp))
    Text(
      text = stringResource(R.string.dialog_error_vpn_text),
      style = MaterialTheme.typography.h4.copy(fontSize = 16.sp),
      color = MaterialTheme.colors.secondary,
      textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(25.dp))
    Row {
      HeaderButton(
        icon = R.drawable.ic_baseline_arrow_back_24,
        contentDescription = "dismiss",
        tooltip = stringResource(R.string.error_btn_dismiss),
        onClick = onDismiss
      )
      HeaderButton(
        icon = R.drawable.ic_baseline_refresh_24,
        contentDescription = "refresh",
        tooltip = stringResource(R.string.error_btn_refresh),
        onClick = onRefresh
      )
    }
  }
}

@Preview
@Composable
fun PreviewErrorContent() {
  CommonPreviewTheme {
    ErrorContent(
      title = "Something went wrong",
      onDismiss = {},
      onRefresh = {}
    )
  }
}