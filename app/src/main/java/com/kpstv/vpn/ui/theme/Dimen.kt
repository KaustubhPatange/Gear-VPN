package com.kpstv.vpn.ui.theme

import android.content.res.Resources
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

private val width = Resources.getSystem().displayMetrics.widthPixels
private val height = Resources.getSystem().displayMetrics.heightPixels

private const val SMALL_SCREEN_PX = 480 * 800

private class ResponsiveDimensions(
    private val valueNormalSize: Dp,
    private val valueSmallSize: Dp
) : ReadOnlyProperty<Any?, Dp> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): Dp {
        return when {
            width * height <= SMALL_SCREEN_PX -> valueSmallSize
            else -> valueNormalSize
        }
    }
}

private class ResponsiveTextDimensions(
    private val valueNormalSize: TextUnit,
    private val valueSmallSize: TextUnit
) : ReadOnlyProperty<Any?, TextUnit> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): TextUnit {
        return when {
            width * height <= SMALL_SCREEN_PX -> valueSmallSize
            else -> valueNormalSize
        }
    }
}

object Dimen {
    /* Dp */
    val dp250 by ResponsiveDimensions(250.dp, 180.dp)
    val dp160 by ResponsiveDimensions(160.dp, 120.dp)
    val dp150 by ResponsiveDimensions(150.dp, 130.dp)
    val dp100 by ResponsiveDimensions(100.dp, 80.dp)
    val dp90 by ResponsiveDimensions(90.dp, 70.dp)
    val dp55 by ResponsiveDimensions(55.dp, 45.dp)
    val dp50 by ResponsiveDimensions(50.dp, 40.dp)
    val dp48 by ResponsiveDimensions(48.dp, 32.dp)
    val dp45 by ResponsiveDimensions(45.dp, 30.dp)
    val dp25 by ResponsiveDimensions(25.dp, 10.dp)
    val dp15 by ResponsiveDimensions(15.dp, 8.dp)
    val dp10 by ResponsiveDimensions(10.dp, 5.dp)
    val dp5 by ResponsiveDimensions(5.dp, 2.dp)

    /* Sp */
    val spNormal by ResponsiveTextDimensions(TextUnit.Unspecified, 13.sp)
    val sp30 by ResponsiveTextDimensions(30.sp, 20.sp)
    val sp35 by ResponsiveTextDimensions(35.sp, 25.sp)
    val sp25 by ResponsiveTextDimensions(25.sp, 18.sp)
    val sp24 by ResponsiveTextDimensions(24.sp, 17.sp)
    val sp20 by ResponsiveTextDimensions(20.sp, 15.sp)
    val sp16 by ResponsiveTextDimensions(16.sp, 12.sp)
    val sp18 by ResponsiveTextDimensions(18.sp, 14.sp)
    val sp15 by ResponsiveTextDimensions(15.sp, 11.sp)
    val sp14 by ResponsiveTextDimensions(14.sp, 11.sp)
    val sp13 by ResponsiveTextDimensions(13.sp, 10.sp)
    val sp12 by ResponsiveTextDimensions(12.sp, 9.sp)
    val sp11 by ResponsiveTextDimensions(11.sp, 8.sp)
}

