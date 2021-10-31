package com.kpstv.vpn.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.kpstv.navigation.compose.DialogRoute
import com.kpstv.navigation.compose.findNavController
import com.kpstv.vpn.R
import com.kpstv.vpn.ui.components.ThemeButton
import com.kpstv.vpn.ui.dialogs.content.FeatureContent
import com.kpstv.vpn.ui.screens.NavigationRoute
import com.kpstv.vpn.ui.theme.CommonPreviewTheme
import kotlinx.parcelize.Parcelize

@Parcelize
object RefreshDialog : DialogRoute

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun HowToRefreshDialog() {
  if (LocalInspectionMode.current) return

  val navController = findNavController(key = NavigationRoute.key)
  navController.CreateDialog(
    key = RefreshDialog::class,
    dialogProperties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    RefreshDialog()
  }
}

@Composable
private fun RefreshDialog() {
  val navController = findNavController(key = NavigationRoute.key)
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(40.dp),
    verticalArrangement = Arrangement.Center
  ) {
    Column(
      modifier = Modifier
        .wrapContentHeight()
        .clip(RoundedCornerShape(5.dp))
        .background(MaterialTheme.colors.background)
    ) {
      RefreshContent()
      Spacer(modifier = Modifier.height(10.dp))
      Divider()
      Spacer(modifier = Modifier.height(15.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
      ) {
        ThemeButton(
          onClick = { navController.closeDialog(RefreshDialog::class) },
          text = stringResource(R.string.alright)
        )
        Spacer(modifier = Modifier.width(20.dp))
      }
      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}

@Composable
private fun RefreshContent() {
  FeatureContent(
    name = stringResource(R.string.dialog_how_to_refresh_title),
    gif = R.drawable.how_to_refresh,
    description = stringResource(R.string.dialog_how_to_refresh_description)
  )
}

@Preview
@Composable
private fun PreviewRefreshContent() {
  CommonPreviewTheme {
    RefreshContent()
  }
}