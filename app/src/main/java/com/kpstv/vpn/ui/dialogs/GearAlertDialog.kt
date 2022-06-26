package com.kpstv.vpn.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.kpstv.navigation.compose.DialogRoute
import com.kpstv.navigation.compose.findNavController
import com.kpstv.vpn.ui.components.ThemeButton
import com.kpstv.vpn.ui.screens.NavigationRoute
import com.kpstv.vpn.ui.theme.CommonPreviewTheme
import kotlin.reflect.KClass

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun GearAlertDialog(
  route: KClass<out DialogRoute>,
  title: String,
  message: String,
  positiveButtonText: String = stringResource(android.R.string.ok),
  onPositiveClick: () -> Unit = {},
  showNegativeButton: Boolean = false,
  negativeButtonText: String = stringResource(android.R.string.cancel),
  onNegativeClick: () -> Unit = {}
) {
  if (LocalInspectionMode.current) return

  val navController = findNavController(key = NavigationRoute.key)
  navController.CreateDialog(
    key = route,
    dialogProperties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 40.dp),
      verticalArrangement = Arrangement.Center
    ) {
      GearAlertDialogContent(
        title = title,
        message = message,
        dismissDialog = { navController.closeDialog(route) },
        positiveButtonText = positiveButtonText,
        negativeButtonText = negativeButtonText,
        onPositiveClick = onPositiveClick,
        onNegativeClick = onNegativeClick,
        showNegativeButton = showNegativeButton
      )
    }
  }
}

@Composable
private fun GearAlertDialogContent(
  title: String,
  message: String,
  dismissDialog: () -> Unit,
  positiveButtonText: String,
  onPositiveClick: () -> Unit,
  showNegativeButton: Boolean,
  negativeButtonText: String,
  onNegativeClick: () -> Unit,
) {
  Column(
    modifier = Modifier
      .wrapContentHeight()
      .clip(RoundedCornerShape(5.dp))
      .background(MaterialTheme.colors.background)
      .padding(20.dp)
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.h5.copy(fontSize = 22.sp)
    )
    Spacer(modifier = Modifier.height(10.dp))
    Text(
      text = message,
      color = MaterialTheme.colors.secondary,
      style = MaterialTheme.typography.h5
    )
    Spacer(modifier = Modifier.height(20.dp))
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.End
    ) {
      if (showNegativeButton) {
        ThemeButton(
          onClick = {
            onNegativeClick()
            dismissDialog()
          },
          text = negativeButtonText
        )
        Spacer(modifier = Modifier.width(20.dp))
      }
      ThemeButton(
        onClick = {
          onPositiveClick()
          dismissDialog()
        },
        text = positiveButtonText
      )
    }
  }
}

@Preview
@Composable
private fun PreviewGearAlertDialog() {
  CommonPreviewTheme {
    GearAlertDialogContent(
      title = "Are you sure?",
      message = "This will perform the action which cannot be undone",
      dismissDialog = { },
      positiveButtonText = stringResource(android.R.string.ok),
      negativeButtonText = stringResource(android.R.string.cancel),
      onPositiveClick = {},
      onNegativeClick = {},
      showNegativeButton = true,
    )
  }
}