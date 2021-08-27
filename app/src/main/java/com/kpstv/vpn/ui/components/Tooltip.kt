package com.kpstv.vpn.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Indication
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.tooltipClickable(
  onClick: () -> Unit,
  onLongClickLabel: String? = null,
  onDoubleClick: (() -> Unit)? = null,
  indication: Indication?,
  interactionSource: MutableInteractionSource,
  enabled: Boolean = true,
  onClickLabel: String? = null,
  role: Role? = null,
  tooltipOffset: IntOffset = IntOffset.Zero,
  tooltipVisibilityMillis: Long = 1000,
  tooltipContent: @Composable () -> Unit,
) : Modifier = composed {

  val haptic = LocalHapticFeedback.current
  val tooltipState = remember { mutableStateOf(false) }

  if (tooltipState.value) {
    Popup(
      popupPositionProvider = TooltipPositionProvider(tooltipOffset),
      onDismissRequest = { tooltipState.value = false }
    ) {
      val alpha = remember { Animatable(0f) }

      Box(modifier = Modifier.graphicsLayer { this.alpha = alpha.value }) {
        tooltipContent()
      }

      LaunchedEffect(Unit) {
        alpha.animateTo(1f)
        delay(tooltipVisibilityMillis)
        alpha.animateTo(0f)
        tooltipState.value = false
      }
    }
  }

  then(combinedClickable(
    interactionSource = interactionSource,
    indication = indication,
    enabled = enabled,
    onClickLabel = onClickLabel,
    role = role,
    onLongClickLabel = onLongClickLabel,
    onLongClick = {
      tooltipState.value = true
      haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    },
    onDoubleClick = onDoubleClick,
    onClick = onClick
  ))
}

private class TooltipPositionProvider(private val offset: IntOffset) : PopupPositionProvider {
  override fun calculatePosition(
    anchorBounds: IntRect,
    windowSize: IntSize,
    layoutDirection: LayoutDirection,
    popupContentSize: IntSize
  ): IntOffset {
    val yPosition = if (anchorBounds.top - popupContentSize.height < 40 /* const offset */) {
      anchorBounds.bottom
    } else {
      anchorBounds.top - popupContentSize.height
    }

    val initialLeft = anchorBounds.left + (anchorBounds.width / 2) - (popupContentSize.width / 2)
    val initialRight = anchorBounds.left + (anchorBounds.width / 2) + (popupContentSize.width / 2)

    val xPosition = if (initialLeft > 0 && initialRight <= windowSize.width) {
      initialLeft
    } else if (anchorBounds.left + popupContentSize.width >= windowSize.width) {
      anchorBounds.right - popupContentSize.width
    } else {
      anchorBounds.left
    }

    return IntOffset(xPosition + offset.x, yPosition + offset.y)
  }
}
