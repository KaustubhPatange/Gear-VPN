package com.kpstv.composetest.ui.components

import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.kpstv.composetest.ui.theme.CommonPreviewTheme

@Composable
fun ThemeButton(onClick: () -> Unit, modifier: Modifier = Modifier, text: String) {
  Button(
    onClick = onClick,
    modifier = modifier
  ) {
    Text(
      text = text,
      color = MaterialTheme.colors.onSecondary
    )
  }
}

@Preview(showBackground = true)
@Composable
fun PreviewThemeButton() {
  CommonPreviewTheme {
    ThemeButton(onClick = {}, text = "BUTTON")
  }
}