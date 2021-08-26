package com.kpstv.vpn.ui.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.kpstv.vpn.R
import com.kpstv.vpn.ui.components.HeaderButton
import com.kpstv.vpn.ui.viewmodels.AppSheetViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberImagePainter
import com.kpstv.navigation.compose.DialogRoute
import com.kpstv.navigation.compose.findController
import com.kpstv.vpn.extensions.drawableFrom
import com.kpstv.vpn.ui.components.ThemeButton
import com.kpstv.vpn.ui.helpers.AppPackage
import com.kpstv.vpn.ui.helpers.Settings
import com.kpstv.vpn.ui.screens.NavigationRoute
import com.kpstv.vpn.ui.theme.CommonPreviewTheme
import com.kpstv.vpn.ui.theme.dotColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.parcelize.Parcelize
import kotlin.math.abs

@Composable
fun AppsDialogMain(onDisallowedAppListChanged: () -> Unit) {
  if (LocalInspectionMode.current) return
  AppsDialog(onDisallowedAppListChanged = onDisallowedAppListChanged)
}

@Parcelize
object AppsDialog : DialogRoute

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AppsDialog(
  viewModel: AppSheetViewModel = viewModel(),
  onDisallowedAppListChanged: () -> Unit
) {
  val controller = findController(key = NavigationRoute.key)
  val context = LocalContext.current

  controller.CreateDialog(
    key = AppsDialog::class,
    dialogProperties = DialogProperties(usePlatformDefaultWidth = false)
  ) { _, dismiss ->

    val disallowedAppPackages = remember { mutableStateListOf<String>() }
    val disallowedAppPackagesSnapshot = arrayListOf<String>()

    LaunchedEffect(Unit) {
      Settings.getDisallowedVpnApps().collect { packages ->
        disallowedAppPackagesSnapshot.addAll(packages)
        disallowedAppPackages.addAll(packages)
      }
    }

    val packages =
      viewModel.get(context).collectAsState(initial = emptyList(), context = Dispatchers.IO)

    BoxWithConstraints(
      modifier = Modifier
        .fillMaxSize()
        .padding(25.dp)
        .clip(RoundedCornerShape(5.dp))
        .background(MaterialTheme.colors.background)
    ) {

      if (packages.value.isEmpty()) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
      }

      LazyColumn(
        modifier = Modifier.padding(horizontal = 17.dp)
      ) {
        itemsIndexed(items = packages.value) { index: Int, item: AppPackage ->

          if (index == 0) {
            key(index) {
              Spacer(modifier = Modifier.height(70.dp))
            }
          }

          key(item.packageName) {

            AppsItem(
              item = item,
              isChecked = !disallowedAppPackages.contains(item.packageName),
              onCheckedChanged = { checked ->
                if (checked) {
                  disallowedAppPackages.remove(item.packageName)
                } else {
                  disallowedAppPackages.add(item.packageName)
                }
              }
            )

          }
        }
      }

      Header(
        filterVpnNumber = abs(disallowedAppPackages.size - packages.value.size),
        onSelectAll = {
          disallowedAppPackages.clear()
        },
        onDeselectAll = {
          disallowedAppPackages.addAll(packages.value.map { it.packageName })
        }
      )

      Footer(
        modifier = Modifier.align(Alignment.BottomCenter),
        onSaveClick = {
          if (disallowedAppPackagesSnapshot != disallowedAppPackages) {
            Settings.setDisallowedVpnApps(disallowedAppPackages.toSet())
            onDisallowedAppListChanged()
          }
          dismiss()
        }
      )

    }
  }
}

@Composable
private fun Header(filterVpnNumber: Int, onSelectAll: () -> Unit, onDeselectAll: () -> Unit) {
  Column(modifier = Modifier.background(color = MaterialTheme.colors.background.copy(alpha = 0.93f))) {
    Spacer(modifier = Modifier.height(12.dp))

    Row(
      modifier = Modifier.padding(horizontal = 15.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.padding(horizontal = 5.dp)) {
        Text(
          text = stringResource(R.string.filter_apps),
          style = MaterialTheme.typography.h4.copy(fontSize = 20.sp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
          text = stringResource(R.string.enable_vpn, filterVpnNumber),
          style = MaterialTheme.typography.subtitle2,
          color = MaterialTheme.colors.onSecondary
        )
      }
      Spacer(modifier = Modifier.weight(1f))
      HeaderButton(
        icon = R.drawable.ic_select_all,
        contentDescription = "select all",
        onClick = { onSelectAll() }
      )
      HeaderButton(
        icon = R.drawable.ic_deselect_all,
        contentDescription = "deselect all",
        onClick = { onDeselectAll() })

    }

    Spacer(modifier = Modifier.height(10.dp))
    Divider()
  }
}

@Composable
private fun Footer(modifier: Modifier = Modifier, onSaveClick: () -> Unit) {
  Column(modifier = modifier.background(color = MaterialTheme.colors.background.copy(alpha = 0.93f))) {
    Divider()
    Spacer(modifier = Modifier.height(10.dp))

    ThemeButton(
      onClick = onSaveClick,
      modifier = Modifier
        .padding(horizontal = 20.dp)
        .height(55.dp)
        .clip(RoundedCornerShape(10.dp))
        .align(Alignment.CenterHorizontally),
      text = stringResource(R.string.save_filters)
    )
    Spacer(modifier = Modifier.height(10.dp))
  }
}

@Composable
private fun AppsItem(
  item: AppPackage,
  isChecked: Boolean,
  onCheckedChanged: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier
      .padding(top = 10.dp)
      .fillMaxWidth()
      .wrapContentHeight()
      .background(MaterialTheme.colors.background)
      .clip(RoundedCornerShape(10.dp))
      .border(
        width = 1.5.dp,
        color = dotColor.copy(alpha = 0.7f),
        shape = RoundedCornerShape(10.dp)
      )
      .clickable(onClick = { onCheckedChanged(!isChecked) })
      .padding(13.dp)
  ) {

    val drawable = remember { item.loadIcon() }

    Image(
      modifier = Modifier.size(45.dp),
      painter = rememberImagePainter(drawable),
      contentDescription = "app icon"
    )
    Spacer(modifier = Modifier.width(15.dp))
    Column(
      modifier = Modifier
        .align(Alignment.CenterVertically)
        .weight(1f)
    ) {
      Text(text = item.name, style = MaterialTheme.typography.h4.copy(fontSize = 16.sp))
      Text(
        text = item.packageName,
        style = MaterialTheme.typography.subtitle2,
        color = MaterialTheme.colors.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
    Spacer(modifier = Modifier.width(15.dp))
    Checkbox(
      modifier = Modifier.align(Alignment.CenterVertically),
      checked = isChecked,
      onCheckedChange = onCheckedChanged
    )
    Spacer(modifier = Modifier.requiredWidth(10.dp))
  }
}

@Preview
@Composable
fun PreviewAppsItem() {
  CommonPreviewTheme {
    val context = LocalContext.current
    AppsItem(
      item = AppPackage(
        name = "Gear VPN",
        packageName = "com.kpstv.vpn",
        loadIcon = { context.drawableFrom(R.mipmap.ic_launcher) }
      ),
      isChecked = true,
      onCheckedChanged = {}
    )
  }
}
