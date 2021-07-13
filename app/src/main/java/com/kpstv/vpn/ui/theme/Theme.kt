package com.kpstv.vpn.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val DarkColorPalette = darkColors(
  primary = primaryColor,
  primaryVariant = primaryVariantColor,
  secondary = secondaryTextColorDark,
  secondaryVariant = secondaryTextColorMoreDark,
  background = background,
  onSecondary = secondaryTextColorDark,
  onSurface = secondaryTextColorMoreDark,
)

private val LightColorPalette = lightColors(
  primary = Purple500,
  primaryVariant = Purple700,
  secondary = Teal200

  /* Other default colors to override
  background = Color.White,
  surface = Color.White,
  onPrimary = Color.White,
  onSecondary = Color.Black,
  onBackground = Color.Black,
  onSurface = Color.Black,
  */
)

@Composable
fun ComposeTestTheme(
//    darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable() () -> Unit
) {
  /*val colors = if (darkTheme) {
      DarkColorPalette
  } else {
      LightColorPalette
  }
*/
  MaterialTheme(
    colors = DarkColorPalette,
    typography = Typography,
    shapes = Shapes,
    content = content
  )
}

@Composable
fun CommonPreviewTheme(content: @Composable () -> Unit) {
  ComposeTestTheme {
    Surface(color = MaterialTheme.colors.background) {
      content()
    }
  }
}