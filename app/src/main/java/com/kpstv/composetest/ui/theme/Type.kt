package com.kpstv.composetest.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.sp
import com.kpstv.composetest.R

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
        fontSize = 25.sp
    ),
    h4 = TextStyle(
        fontFamily = jostMedium,
        fontSize = 24.sp
    ),
    h5 = TextStyle(
        fontFamily = jostMedium,
        fontWeight = FontWeight.W700,
        fontSize = 16.sp
    ),
    subtitle1 = TextStyle(
        fontFamily = jostBold,
        letterSpacing = TextUnit(0.8f, TextUnitType.Sp),
        fontSize = 14.sp
    ),
    subtitle2 = TextStyle(
        fontFamily = jostBold,
        letterSpacing = TextUnit(0.8f, TextUnitType.Sp),
        fontSize = 13.sp
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