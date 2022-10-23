package com.kpstv.vpn.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.kpstv.vpn.ui.theme.CommonPreviewTheme
import com.kpstv.vpn.ui.theme.Dimen.spNormal

@Composable
fun ThemeButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  text: String,
  fontSize: TextUnit = spNormal,
  enabled: Boolean = true
) {
  Button(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,

    colors = ButtonDefaults.buttonColors(
      disabledBackgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.4f),
    )
  ) {
    Text(
      fontSize = fontSize,
      text = text,
      color = if (enabled) MaterialTheme.colors.onSecondary else MaterialTheme.colors.onSecondary.copy(
        alpha = 0.5f
      )
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
