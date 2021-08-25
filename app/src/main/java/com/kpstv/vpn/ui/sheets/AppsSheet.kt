package com.kpstv.vpn.ui.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.navigationBarsPadding
import com.kpstv.vpn.R
import com.kpstv.vpn.ui.components.BottomSheetState
import com.kpstv.vpn.ui.components.HeaderButton
import com.kpstv.vpn.ui.components.rememberBottomSheetState

@Composable
fun AppsSheetServer(appSheetState: BottomSheetState) {
  AppsSheet(appSheetState = appSheetState)
}

@Composable
fun AppsSheet(
  appSheetState: BottomSheetState,
) {
  BaseBottomSheet(bottomSheetState = appSheetState) {
    Column(
      modifier = Modifier
        .padding(horizontal = 5.dp)
        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
        .background(MaterialTheme.colors.background)
        .navigationBarsPadding()
        .padding(12.dp)
    ) {
      Row(horizontalArrangement = Arrangement.SpaceBetween) {
        Column {
          Text(text = stringResource(R.string.filter_apps))
          Text(text = stringResource(R.string.enable_vpn, 2))
        }
        Row {
          HeaderButton(
            icon = R.drawable.ic_select_all,
            contentDescription = "select all",
            onClick = {
              //TODO:
            }
          )
          HeaderButton(
            icon = R.drawable.ic_deselect_all,
            contentDescription = "deselect all",
            onClick = {
            // TODO:
          })
        }
      }
      Spacer(modifier = Modifier.height(10.dp))
      Divider()


    }
  }
}

@Preview(showBackground = true)
@Composable
fun PreviewAppsSheet() {
  AppsSheet(appSheetState = rememberBottomSheetState())
}