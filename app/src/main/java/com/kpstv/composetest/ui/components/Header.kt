package com.kpstv.composetest.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.statusBarsPadding
import com.kpstv.composetest.R
import com.kpstv.composetest.ui.theme.CommonPreviewTheme

@Composable
fun Header(title: String, onBackButton: () -> Unit = {}) {
  Column(
    modifier = Modifier
      .background(color = MaterialTheme.colors.background.copy(alpha = 0.93f))
      .statusBarsPadding()
      .padding(top = 10.dp)
  ) {
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)) {
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
    }
    Spacer(modifier = Modifier.height(10.dp))
    Divider(color = MaterialTheme.colors.primaryVariant, thickness = 1.dp)
  }
}

@Preview(showBackground = true)
@Composable
fun PreviewHeader() {
  CommonPreviewTheme {
    Header(title = "Test title")
  }
}