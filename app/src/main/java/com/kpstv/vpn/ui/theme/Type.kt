package com.kpstv.vpn.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.sp
import com.kpstv.vpn.R
import com.kpstv.vpn.ui.theme.Dimen.sp13
import com.kpstv.vpn.ui.theme.Dimen.sp14
import com.kpstv.vpn.ui.theme.Dimen.sp16
import com.kpstv.vpn.ui.theme.Dimen.sp24
import com.kpstv.vpn.ui.theme.Dimen.sp25

val jostRegular = FontFamily(
  Font(R.font.jost_regular)
)

val jostMedium = FontFamily(
  Font(R.font.jost_medium)
)

val jostBold = FontFamily(
  Font(R.font.jost_bold)
)

@OptIn(ExperimentalUnitApi::class)
val Typography = Typography(
  h2 = TextStyle(
    fontFamily = jostRegular,
    fontSize = sp25
  ),
  h4 = TextStyle(
    fontFamily = jostMedium,
    fontSize = sp24
  ),
  h5 = TextStyle(
    fontFamily = jostMedium,
    fontWeight = FontWeight.W700,
    fontSize = sp16
  ),
  subtitle1 = TextStyle(
    fontFamily = jostBold,
    letterSpacing = TextUnit(0.8f, TextUnitType.Sp),
    fontSize = sp14
  ),
  subtitle2 = TextStyle(
    fontFamily = jostBold,
    letterSpacing = TextUnit(0.8f, TextUnitType.Sp),
    fontSize = sp13
  ),
  button = TextStyle(
    fontFamily = jostMedium,
    fontWeight = FontWeight.W700,
    fontSize = 14.sp
  )
  /* body1 = TextStyle(
       fontFamily = joshVariable,
       fontWeight = FontWeight.Normal,
       fontSize = 16.sp
   )*/
  /* Other default text styles to override
  button = TextStyle(
      fontFamily = FontFamily.Default,
      fontWeight = FontWeight.W500,
      fontSize = 14.sp
  ),
  caption = TextStyle(
      fontFamily = FontFamily.Default,
      fontWeight = FontWeight.Normal,
      fontSize = 12.sp
  )
  */
)