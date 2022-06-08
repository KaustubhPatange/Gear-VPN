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
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
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
import kotlin.math.roundToInt

@Composable
fun rememberBottomSheetState(isExpanded: Boolean = false) =
  rememberSaveable(isExpanded, saver = BottomSheetState.Saver) {
    BottomSheetState(isExpanded)
  }

class BottomSheetState(isExpanded: Boolean = false) {
  internal val expanded = mutableStateOf(isExpanded)

  fun show() {
    expanded.value = true
  }

  fun hide() {
    expanded.value = false
  }

  fun isVisible() = expanded.value

  companion object {
    val Saver: Saver<BottomSheetState, *> = Saver(
      save = { it.expanded },
      restore = { BottomSheetState(it.value) }
    )
  }
}

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
  bottomSheetState: BottomSheetState,
  content: @Composable () -> Unit,
) {
  val background = animateColorAsState(
    targetValue = if (bottomSheetState.isVisible())
      Color.Black.copy(alpha = 0.5f)
    else Color.Transparent
  )

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .background(background.value)
  ) {
    val boxHeightPx = with(LocalDensity.current) { maxHeight.toPx() }

    val swipeableState = rememberSwipeableState(bottomSheetState.expanded.value) state@{ state ->
      if (bottomSheetState.expanded.value != state) {
        bottomSheetState.hide()
      }
      return@state true
    }

    val sheetSizePx = remember { mutableStateOf(0f) }
    val anchors = mapOf(
      boxHeightPx to false,
      (1f + boxHeightPx - sheetSizePx.value) to true // if 1f is removed it throws a runtime exception.
    )

    LaunchedEffect(bottomSheetState.expanded.value) effect@{
      swipeableState.animateTo(
        targetValue = bottomSheetState.isVisible(),
        anim = tween()
      )
    }

    if (bottomSheetState.isVisible()) {
      Box(modifier = Modifier
        .fillMaxSize()
        .pointerInteropFilter pointer@{ event ->
          if (event.action == MotionEvent.ACTION_UP) {
            bottomSheetState.hide()
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

/*enum class BottomSheetState(val state: Int) {
  Collapsed(0), Expanded(1)
}*/

@Preview
@Composable
fun PreviewBottomSheetPlayground() {
  CommonPreviewTheme {
    val context = LocalContext.current

    Box {
      val bottomSheetState = rememberBottomSheetState()

      Button(onClick = {
        if (bottomSheetState.isVisible()) {
          bottomSheetState.hide()
        } else {
          bottomSheetState.show()
        }
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