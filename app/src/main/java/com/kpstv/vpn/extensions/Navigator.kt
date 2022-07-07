package com.kpstv.vpn.extensions

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.kpstv.navigation.compose.*

val SlideTop get() = SlideTopTransition.key

val SlideTopTransition = object : NavigatorTransition() {
  override val key: TransitionKey = TransitionKey("slide_top_transition")
  override val forwardTransition: ComposeTransition = ComposeTransition { modifier, _, height, progress ->
    modifier.then(
      Modifier.graphicsLayer {
        translationY = height + (-1) * height * progress
        alpha = progress
      }
    )
  }
  override val backwardTransition: ComposeTransition = ComposeTransition { modifier, _, height, progress ->
    modifier.then(
      Modifier.graphicsLayer {
        translationY = height * progress
        alpha = 1 - progress
      }
    )
  }
}

fun <T : Route> ComposeNavigator.Controller<T>.isDialogShowing(dialogRoute: DialogRoute) : Boolean {
  return getAllDialogHistory().contains(dialogRoute)
}