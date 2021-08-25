package com.kpstv.vpn.ui.sheets

import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalInspectionMode
import com.kpstv.navigation.compose.findComposeNavigator
import com.kpstv.vpn.ui.components.BottomSheet
import com.kpstv.vpn.ui.components.BottomSheetState

@Composable
fun BaseBottomSheet(
  bottomSheetState: BottomSheetState,
  content: @Composable () -> Unit
) {
  BottomSheet(bottomSheetState = bottomSheetState) {

    content()

    if (LocalInspectionMode.current) return@BottomSheet

    val navigator = findComposeNavigator()

    val backpress = remember {
      object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
          bottomSheetState.hide()
        }
      }
    }

    LaunchedEffect(bottomSheetState.expanded.value) {
      navigator.suppressBackPress = bottomSheetState.isVisible()
      backpress.isEnabled = bottomSheetState.isVisible()
    }

    val dispatcher = checkNotNull(LocalOnBackPressedDispatcherOwner.current).onBackPressedDispatcher
    DisposableEffect(Unit) {
      dispatcher.addCallback(backpress)
      onDispose {
        backpress.remove()
      }
    }
  }
}