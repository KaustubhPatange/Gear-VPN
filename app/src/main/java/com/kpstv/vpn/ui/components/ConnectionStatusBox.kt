package com.kpstv.vpn.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.google.accompanist.insets.statusBarsHeight
import com.kpstv.vpn.extensions.utils.NetworkMonitor

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ConnectionStatusBox() {
  val connectivityStatus = NetworkMonitor.connection.collectAsState()

  AnimatedVisibility(visible = !connectivityStatus.value, enter = fadeIn(), exit = fadeOut()) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsHeight()
        .background(MaterialTheme.colors.error)
        .animateContentSize()
    )
  }
}