package com.kpstv.vpn.ui.components

import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.google.accompanist.insets.navigationBarsPadding
import com.kpstv.vpn.R
import com.kpstv.vpn.ui.theme.CommonPreviewTheme
import com.kpstv.vpn.ui.theme.backgroundInner
import com.kpstv.vpn.ui.theme.dotColor

enum class ProtocolConnectionType {
  TCP, UDP
}

@Composable
fun ProtocolSheet(
  protocolSheetState: MutableState<BottomSheetState>,
  enableTCP: Boolean = true,
  enableUDP: Boolean = true,
  onItemClick: (ProtocolConnectionType) -> Unit,
  suppressBackPress: (Boolean) -> Unit
) {
  BottomSheet(bottomSheetState = protocolSheetState) {

    CommonSheet(enableTCP = enableTCP, enableUDP = enableUDP, onItemClick = onItemClick)

    val backpress = remember {
      object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
          protocolSheetState.value = BottomSheetState.Collapsed
        }
      }
    }

    LaunchedEffect(protocolSheetState.value) {
      suppressBackPress.invoke(protocolSheetState.value == BottomSheetState.Expanded)
      backpress.isEnabled = protocolSheetState.value == BottomSheetState.Expanded
    }

    val dispatcher = checkNotNull(LocalOnBackPressedDispatcherOwner.current).onBackPressedDispatcher
    DisposableEffect(Unit) {
      dispatcher.addCallback(backpress)
      onDispose {
        backpress.remove()
      }
    }
  }
}

@Composable
private fun CommonSheet(
  enableTCP: Boolean,
  enableUDP: Boolean,
  onItemClick: (ProtocolConnectionType) -> Unit
) {
  Column(
    modifier = Modifier
      .padding(horizontal = 5.dp)
      .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
      .background(MaterialTheme.colors.background)
      .navigationBarsPadding()
      .padding(12.dp)
  ) {
    Text(
      text = stringResource(R.string.choose_protocol),
      style = MaterialTheme.typography.h4.copy(fontSize = 20.sp)
    )
    Spacer(modifier = Modifier.height(10.dp))
    Divider()
    Spacer(modifier = Modifier.height(20.dp))

    ConstraintLayout {
      val (row1, row2) = createRefs()
      CommonRow(
        modifier = Modifier
          .constrainAs(row1) {
            start.linkTo(parent.start)
            bottom.linkTo(parent.bottom)
            top.linkTo(parent.top)
            end.linkTo(row2.start)
            height = Dimension.fillToConstraints
          }
          .fillMaxWidth(0.5f)
          .padding(start = 5.dp, end = 8.dp),
        enable = enableTCP,
        title = "TCP",
        description = stringResource(R.string.optimal_tcp),
        onClick = { onItemClick.invoke(ProtocolConnectionType.TCP) }
      )
      CommonRow(
        modifier = Modifier
          .constrainAs(row2) {
            start.linkTo(row1.end)
            bottom.linkTo(parent.bottom)
            top.linkTo(parent.top)
            end.linkTo(parent.end)
          }
          .fillMaxWidth(0.5f)
          .padding(start = 8.dp, end = 5.dp),
        enable = enableUDP,
        title = "UDP",
        description = stringResource(R.string.optimal_udp),
        onClick = { onItemClick.invoke(ProtocolConnectionType.UDP) }
      )
    }
  }
}

@Composable
private fun CommonRow(
  modifier: Modifier = Modifier,
  enable: Boolean = true,
  title: String,
  description: String,
  onClick: () -> Unit
) {
  Column(
    modifier = modifier
      .clip(RoundedCornerShape(10.dp))
      .then(
        if (enable) {
          Modifier
            .border(1.dp, color = dotColor, shape = RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
        } else {
          Modifier
            .border(
              1.dp,
              color = MaterialTheme.colors.primaryVariant,
              shape = RoundedCornerShape(10.dp)
            )
            .background(color = Color.Black.copy(alpha = 0.1f))
        }
      )
      .padding(10.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    CompositionLocalProvider(LocalContentAlpha provides if (enable) 1f else 0.3f) {
      Text(
        text = title,
        style = MaterialTheme.typography.h4.copy(fontSize = 26.sp)
      )
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = description,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.onSecondary.copy(alpha = LocalContentAlpha.current),
        style = MaterialTheme.typography.h4.copy(fontSize = 15.sp)
      )
    }
  }
}

@Preview
@Composable
fun PreviewProtocolSheet() {
  CommonPreviewTheme {
    CommonSheet(onItemClick = {}, enableTCP = true, enableUDP = true)
  }
}

@Preview
@Composable
fun PreviewProtocolSheet2() {
  CommonPreviewTheme {
    CommonSheet(onItemClick = {}, enableTCP = true, enableUDP = false)
  }
}