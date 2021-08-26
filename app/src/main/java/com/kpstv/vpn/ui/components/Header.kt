package com.kpstv.vpn.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.statusBarsPadding
import com.kpstv.vpn.R
import com.kpstv.vpn.ui.theme.CommonPreviewTheme

@Composable
fun Header(title: String, onBackButton: () -> Unit = {}, actionRow: @Composable () -> Unit = {}) {
  Column(
    modifier = Modifier
      .background(color = MaterialTheme.colors.background.copy(alpha = 0.93f))
      .statusBarsPadding()
      .padding(top = 5.dp)
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

@Composable
fun HeaderButton(@DrawableRes icon: Int, contentDescription: String?, onClick: () -> Unit) {
  IconButton(
    onClick = onClick,
    modifier = Modifier
      .clip(CircleShape)
  ) {
    Icon(
      painter = painterResource(icon),
      contentDescription = contentDescription
    )
  }
}

@Preview
@Composable
fun PreviewHeader() {
  CommonPreviewTheme {
    Header(title = "Test title")
  }
}