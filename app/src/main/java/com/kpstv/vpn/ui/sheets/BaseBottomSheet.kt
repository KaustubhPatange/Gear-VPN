package com.kpstv.vpn.ui.sheets

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalInspectionMode
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

    BackHandler(bottomSheetState.expanded.value) {
      bottomSheetState.hide()
    }
  }
}