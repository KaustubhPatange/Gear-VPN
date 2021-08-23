package com.kpstv.vpn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kpstv.navigation.compose.DialogRoute
import com.kpstv.navigation.compose.findController
import com.kpstv.vpn.R
import com.kpstv.vpn.ui.screens.NavigationRoute
import com.kpstv.vpn.ui.theme.CommonPreviewTheme
import kotlinx.parcelize.Parcelize

@Parcelize
object NoServerDialog : DialogRoute

@Composable
fun EmptyVpnDialog(show: Boolean = false) {
  val controller = findController(NavigationRoute::class)

  controller.CreateDialog(key = NoServerDialog::class) { _, dismiss ->
    DialogContent(dismiss = dismiss)
  }

  if (show) controller.showDialog(NoServerDialog)
}

@Composable
private fun DialogContent(dismiss: () -> Unit) {
  Column(modifier = Modifier.background(MaterialTheme.colors.background).padding(15.dp)) {
    Text(
      text = stringResource(R.string.dialog_empty_vpn_text),
      style = MaterialTheme.typography.h4.copy(fontSize = 18.sp)
    )
    Spacer(modifier = Modifier.height(15.dp))
    ThemeButton(
      modifier = Modifier.align(Alignment.End),
      onClick = dismiss,
      text = stringResource(R.string.alright)
    )
  }
}

@Preview(showBackground = true)
@Composable
fun PreviewEmptyVpnDialog() {
  CommonPreviewTheme {
    DialogContent {}
  }
}
