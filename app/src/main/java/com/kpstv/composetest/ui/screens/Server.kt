package com.kpstv.composetest.ui.screens

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.coil.rememberCoilPainter
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.kpstv.composetest.R
import com.kpstv.composetest.data.db.repository.VpnLoadState
import com.kpstv.composetest.data.models.VpnConfiguration
import com.kpstv.composetest.extensions.utils.FlagUtils
import com.kpstv.composetest.ui.components.Header
import com.kpstv.composetest.ui.components.ThemeButton
import com.kpstv.composetest.ui.theme.CommonPreviewTheme
import com.kpstv.composetest.ui.theme.dotColor
import com.kpstv.composetest.ui.theme.goldenYellow

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ServerScreen(
  vpnState: VpnLoadState,
  onBackButton: () -> Unit = {},
  onRefresh: () -> Unit = {},
  onImportButton: () -> Unit = {},
  onItemClick: (VpnConfiguration) -> Unit
) {
  val swipeRefreshState = rememberSwipeRefreshState(vpnState is VpnLoadState.Loading)

  SwipeRefresh(
    modifier = Modifier
      .fillMaxSize(),
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
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {

      LazyColumn(
        modifier = Modifier
          .padding(horizontal = 20.dp)
      ) {
        itemsIndexed(vpnState.configs) { index, item ->
          if (index == 0) {
            Spacer(
              modifier = Modifier
                .statusBarsPadding()
                .height(80.dp)
            )
            ServerHeader(title = stringResource(R.string.premium_server), premium = true)
            Spacer(modifier = Modifier.height(15.dp))
          }
          if (index == 3) {
            Spacer(modifier = Modifier.height(15.dp))
            ServerHeader(title = stringResource(R.string.free_server))
            Spacer(modifier = Modifier.height(10.dp))
          }
          CommonItem(config = item, onClick = onItemClick)

          if (index == vpnState.configs.size - 1) {
            Spacer(
              modifier = Modifier
                .height(100.dp)
                .navigationBarsPadding()
            )
          }
        }
      }

      Header(title = stringResource(R.string.choose_server), onBackButton = onBackButton)

      Footer(
        modifier = Modifier.align(Alignment.BottomCenter),
        onImportButton = onImportButton
      )
    }
  }
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
    Divider(color = MaterialTheme.colors.primaryVariant, thickness = 1.dp)

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
private fun ServerHeader(title: String, premium: Boolean = false) {
  Row {
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
  onClick: (VpnConfiguration) -> Unit = {}
) {
  Spacer(modifier = Modifier.height(5.dp))

  Row(
    modifier = Modifier
      .border(
        width = 1.5.dp,
        color = if (config.premium) goldenYellow else dotColor.copy(alpha = 0.7f),
        shape = RoundedCornerShape(10.dp)
      )
      .height(65.dp)
      .clickable(
        onClick = { onClick.invoke(config) },
        /*interactionSource = remember { MutableInteractionSource() },
        indication = rememberRipple(radius = 10.dp)*/
      )
      .padding(5.dp)
      .fillMaxWidth()
  ) {
    if (config.countryFlagUrl.isNotEmpty()) {
      Image(
        painter = rememberCoilPainter(
          FlagUtils.getOrNull(config.country) ?: config.countryFlagUrl,
          fadeIn = true
        ),
        modifier = Modifier
          .padding(5.dp)
          .requiredWidthIn(max = 40.dp)
          .fillMaxHeight()
//          .clip(shape = CircleShape)
          .scale(1f),
        contentDescription = "Country flag",
        contentScale = ContentScale.Fit
      )
    } else {
      Spacer(modifier = Modifier.width(50.dp))
    }

    Spacer(modifier = Modifier.width(10.dp))

    Column(
      modifier = Modifier
        .weight(1f)
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
          color = MaterialTheme.colors.onSecondary
        )
      }
      Spacer(modifier = Modifier.height(1.dp))
      Text(
        text = stringResource(
          R.string.server_subtitle,
          config.sessions,
          config.upTime,
          config.speed
        ),
        style = MaterialTheme.typography.subtitle2,
        color = MaterialTheme.colors.onSurface,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
      )
    }
  }

  Spacer(modifier = Modifier.height(5.dp))
}

@Preview(showBackground = true)
@Composable
fun PreviewServerScreen() {
  CommonPreviewTheme {
    ServerScreen(vpnState = VpnLoadState.Loading(), onItemClick = {})
  }
}

@Preview(showBackground = true)
@Composable
fun PreviewCommonItem() {
  CommonPreviewTheme {
    CommonItem(
      config = createTestConfiguration()
    )
  }
}

@Preview(showBackground = true)
@Composable
fun PreviewCommonItemPremium() {
  CommonPreviewTheme {
    CommonItem(
      config = createTestConfiguration().copy(premium = true)
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
    speed = "73.24"
  )