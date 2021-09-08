package com.kpstv.vpn.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.insets.statusBarsPadding
import com.kpstv.vpn.R
import com.kpstv.vpn.ui.theme.CommonPreviewTheme

@Composable
fun Header(title: String, modifier: Modifier = Modifier, onBackButton: () -> Unit = {}, actionRow: @Composable () -> Unit = {}) {
  Column(
    modifier = modifier
      .background(color = MaterialTheme.colors.background.copy(alpha = 0.93f))
      .statusBarsPadding()
  ) {

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 10.dp)
    ) {
      HeaderButton(
        icon = R.drawable.ic_baseline_arrow_back_24,
        contentDescription = "back button",
        onClick = onBackButton
      )
      IconButton(
        onClick = onBackButton,
        modifier = Modifier
          .clip(CircleShape)
      ) {
        Image(
          painter = painterResource(R.drawable.ic_baseline_arrow_back_24),
          contentDescription = "back button"
        )
      }
      Text(
        text = title,
        modifier = Modifier.align(Alignment.Center),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.h4
      )
      Box(modifier = Modifier.align(Alignment.CenterEnd)) {
        actionRow()
      }
    }
    Spacer(modifier = Modifier.height(10.dp))
    Divider(color = MaterialTheme.colors.primaryVariant)
  }
}

@OptIn(
  ExperimentalComposeUiApi::class,
  androidx.compose.foundation.ExperimentalFoundationApi::class
)
@Composable
fun HeaderButton(
  @DrawableRes icon: Int,
  modifier: Modifier = Modifier,
  iconTint: Color = LocalContentColor.current.copy(alpha = LocalContentAlpha.current),
  contentDescription: String?,
  tooltip: String? = null,
  tooltipOffset: IntOffset = IntOffset.Zero,
  enabled: Boolean = true,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  onClick: () -> Unit
) {

  Box(
    modifier = modifier
      .tooltipClickable(
        onClick = onClick,
        enabled = enabled,
        role = Role.Button,
        interactionSource = interactionSource,
        indication = rememberRipple(bounded = false, radius = 24.dp),
        tooltipContent = {
          if (tooltip != null) {
            TooltipText(text = tooltip)
          }
        },
        tooltipOffset = tooltipOffset
      )
      .size(48.dp)
      .clip(CircleShape),
    contentAlignment = Alignment.Center
  ) {
    val contentAlpha = if (enabled) LocalContentAlpha.current else ContentAlpha.disabled
    CompositionLocalProvider(LocalContentAlpha provides contentAlpha) {
      Icon(
        painter = painterResource(icon),
        tint = iconTint,
        contentDescription = contentDescription
      )
    }
  }
}

@Preview
@Composable
fun PreviewHeader() {
  CommonPreviewTheme {
    Header(title = "Test title")
  }
}