package com.kpstv.vpn.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.kpstv.vpn.R
import com.kpstv.vpn.ui.theme.CommonPreviewTheme
import com.kpstv.vpn.ui.theme.Dimen.spNormal

@Composable
fun ThemeButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  text: String,
  fontSize: TextUnit = spNormal,
  backgroundColor: Color = MaterialTheme.colors.primary,
  textColor: Color = MaterialTheme.colors.onSecondary,
  enabled: Boolean = true
) {
  Button(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    colors = ButtonDefaults.buttonColors(
      backgroundColor = backgroundColor,
      disabledBackgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.4f),
    )
  ) {
    Text(
      fontSize = fontSize,
      text = text,
      color = if (enabled) textColor else textColor.copy(
        alpha = 0.5f
      )
    )
  }
}

@Composable
fun ThemeOutlinedButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  text: String,
  fontSize: TextUnit = spNormal,
  borderColor: Color = MaterialTheme.colors.onSurface,
  textColor: Color = MaterialTheme.colors.onSecondary,
  enabled: Boolean = true
) {
  OutlinedButton(
    modifier = modifier,
    colors = ButtonDefaults.outlinedButtonColors(
      backgroundColor = Color.Transparent,
    ),
    border = BorderStroke(
      width = ButtonDefaults.OutlinedBorderSize,
      color = if (enabled) borderColor else borderColor.copy(
        ButtonDefaults.OutlinedBorderOpacity
      )
    ),
    onClick = onClick
  ) {
    Text(
      fontSize = fontSize,
      text = text,
      color = if (enabled) textColor else textColor.copy(
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

@Preview
@Composable
fun PreviewThemeOutlinedButton() {
  CommonPreviewTheme {
    Column {
      ThemeOutlinedButton(onClick = {}, text = "BUTTON")
      Spacer(modifier = Modifier.height(20.dp))
      ThemeOutlinedButton(onClick = {}, text = "BUTTON", enabled = false)
    }
  }
}
