package com.kpstv.vpn.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kpstv.vpn.ui.theme.CommonPreviewTheme

@Composable
fun ThemeButton(onClick: () -> Unit, modifier: Modifier = Modifier, text: String, enabled: Boolean = true) {
  Button(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,

    colors = ButtonDefaults.buttonColors(
      disabledBackgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.4f),
    )
  ) {
    Text(
      text = text,
      color = if (enabled) MaterialTheme.colors.onSecondary else MaterialTheme.colors.onSecondary.copy(alpha = 0.5f)
    )
  }
}

@Preview
@Composable
fun PreviewThemeButton() {
  CommonPreviewTheme {
    Column {
      ThemeButton(onClick = {}, text = "BUTTON")
      Spacer(modifier = Modifier.height(20.dp))
      ThemeButton(onClick = {}, text = "BUTTON", enabled = false)
    }
  }
}
