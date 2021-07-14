package com.kpstv.vpn.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
fun <T> AnimatedSwipeDismiss(
  modifier: Modifier = Modifier,
  item: T,
  background: @Composable (isDismissed: Boolean) -> Unit,
  content: @Composable (isDismissed: Boolean) -> Unit,
  directions: Set<DismissDirection> = setOf(DismissDirection.EndToStart),
  enter: EnterTransition = EnterTransition.None,
  exit: ExitTransition = shrinkVertically(
    animationSpec = tween(
      durationMillis = 500,
    )
  ),
  onDismiss: (T) -> Unit
) {
  val dismissState = rememberDismissState()
  val isDismissed = dismissState.isDismissed(DismissDirection.EndToStart)

  LaunchedEffect(dismissState.currentValue) {
    if (dismissState.currentValue == DismissValue.DismissedToStart) {
      delay(600)
      onDismiss(item)
    }
  }

  AnimatedVisibility(
    modifier = modifier,
    visible = !isDismissed,
    enter = enter,
    exit = exit
  ) {
    SwipeToDismiss(
      modifier = modifier,
      state = dismissState,
      directions = directions,
      background = { background(isDismissed) },
      dismissContent = { content(isDismissed) }
    )
  }
}