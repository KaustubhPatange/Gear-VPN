package com.kpstv.vpn.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.kpstv.vpn.ui.theme.foregroundColor as foreColor

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

interface Attrs {
  val errorButton: Color
  val foregroundColor: Color
}

private class DarkAttrsColor : Attrs {
  override val errorButton: Color = Color(0xFFBB2020)
  override val foregroundColor: Color = foreColor
}

private class GearVPNColors {
  val defaults @Composable get() = MaterialTheme.colors
  val custom @Composable get() : Attrs = LocalGearVPNColors.current
}

private val LocalGearVPNColors = staticCompositionLocalOf<Attrs> { DarkAttrsColor() }

object GearVPNTheme {
  val colors @Composable get() = LocalGearVPNColors.current
}

@Composable
fun ComposeTestTheme(
//    darkTheme: Boolean = isSystemInDarkTheme(),
  attrsColor: Attrs = DarkAttrsColor(),
  content: @Composable () -> Unit,
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
    shapes = Shapes
  ) {
    CompositionLocalProvider(LocalGearVPNColors provides attrsColor) {
      content()
    }
  }
}

@Composable
fun CommonPreviewTheme(content: @Composable () -> Unit) {
  ComposeTestTheme {
    Surface(color = MaterialTheme.colors.background) {
      content()
    }
  }
}
