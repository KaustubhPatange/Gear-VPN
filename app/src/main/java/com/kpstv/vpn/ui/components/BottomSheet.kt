package com.kpstv.vpn.ui.components

import android.view.MotionEvent
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.kpstv.vpn.ui.theme.CommonPreviewTheme
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun rememberBottomSheetState(initialState: BottomSheetState = BottomSheetState.Collapsed) =
  remember { mutableStateOf(initialState) }

/**
 * A bottom sheet component to display Modal Bottom Sheet.
 *
 * The one provided by material design needs a nesting. This doesn't, so you can stack
 * countless bottom sheet while getting similar behavior when done through the view system.
 *
 * The only problem is the composition is drawn on the screen even though the state
 * is collapsed.
 *
 * Note: Make sure the component is a child of Box or any layout stack content above
 * one another.
 */
@OptIn(ExperimentalMaterialApi::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun BottomSheet(
  bottomSheetState: MutableState<BottomSheetState>,
  content: @Composable () -> Unit,
) {
  val background = animateColorAsState(
    targetValue = if (bottomSheetState.value == BottomSheetState.Expanded)
      Color.Black.copy(alpha = 0.5f)
    else Color.Transparent
  )

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .background(background.value)
  ) {
    val boxHeightPx = with(LocalDensity.current) { maxHeight.toPx() }

    val swipeableState = rememberSwipeableState(bottomSheetState.value.state) state@{ state ->
      if (bottomSheetState.value.state != state) {
        bottomSheetState.value = BottomSheetState.Collapsed
      }
      return@state true
    }

    val sheetSizePx = remember { mutableStateOf(0f) }
    val anchors = mapOf(
      boxHeightPx to 0,
      (1f + boxHeightPx - sheetSizePx.value) to 1 // if 1f is removed it throws a runtime exception.
    )

    LaunchedEffect(bottomSheetState.value) effect@{
      if (swipeableState.isAnimationRunning) return@effect
      swipeableState.animateTo(
        targetValue = if (bottomSheetState.value == BottomSheetState.Expanded) 1 else 0,
        anim = tween()
      )
    }

    if (bottomSheetState.value == BottomSheetState.Expanded) {
      Box(modifier = Modifier
        .fillMaxSize()
        .pointerInteropFilter pointer@{ event ->
          if (event.action == MotionEvent.ACTION_UP) {
            bottomSheetState.value = BottomSheetState.Collapsed
          }
          return@pointer true
        })
    }
    Layout(
      modifier = Modifier
        .offset { IntOffset(0, swipeableState.offset.value.roundToInt()) }
        .swipeable(
          state = swipeableState,
          anchors = anchors,
          thresholds = { _, _ -> FractionalThreshold(0.5f) },
          orientation = Orientation.Vertical
        ),
      content = content
    ) { measurables, constraints ->
      val placeables = measurables.map { measurable ->
        measurable.measure(constraints)
      }
      sheetSizePx.value = placeables.maxOf { it.height.toFloat() }
      layout(constraints.maxWidth, constraints.maxHeight) {
        placeables.forEach { placeable ->
          placeable.placeRelative(0, 0)
        }
      }
    }
  }
}

enum class BottomSheetState(val state: Int) {
  Collapsed(0), Expanded(1)
}

@Preview(showBackground = true)
@Composable
fun PreviewBottomSheetPlayground() {
  CommonPreviewTheme {
    val context = LocalContext.current

    Box {
      val bottomSheetState = rememberBottomSheetState()

      Button(onClick = {
        bottomSheetState.value = if (bottomSheetState.value == BottomSheetState.Expanded)
          BottomSheetState.Collapsed
        else
          BottomSheetState.Expanded
      }) {
        Text(text = "Toggle Sheet")
      }

      BottomSheet(bottomSheetState = bottomSheetState) {
        Box(
          modifier = Modifier
            .padding(horizontal = 10.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
            .background(Color.DarkGray)
        ) {
          Button(
            onClick = { Toast.makeText(context, "Hello world", Toast.LENGTH_SHORT).show() }
          ) {
            Text(text = "Hello world")
          }
          Spacer(modifier = Modifier.height(200.dp))
        }
      }
    }
  }
}