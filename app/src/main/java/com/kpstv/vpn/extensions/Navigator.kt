package com.kpstv.vpn.extensions

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.kpstv.navigation.compose.ComposeTransition
import com.kpstv.navigation.compose.NavigatorTransition

val SlideTop get() = SlideTopTransition.key

val SlideTopTransition = object : NavigatorTransition() {
  override val forwardTransition: ComposeTransition = ComposeTransition { modifier, _, height, progress ->
    modifier.then(
      Modifier.graphicsLayer {
        translationY = height + (-1) * height * progress
        alpha = progress
      }
    )
  }
  override val backwardTransition: ComposeTransition = ComposeTransition { modifier, width, height, progress ->
    modifier.then(
      Modifier.graphicsLayer {
        translationY = height * progress
        alpha = 1 - progress
      }
    )
  }
}