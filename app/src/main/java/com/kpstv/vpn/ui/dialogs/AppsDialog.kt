package com.kpstv.vpn.ui.dialogs

import android.os.Parcelable
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberImagePainter
import com.kpstv.navigation.compose.DialogRoute
import com.kpstv.navigation.compose.findController
import com.kpstv.vpn.R
import com.kpstv.vpn.extensions.drawableFrom
import com.kpstv.vpn.ui.components.AutoSizeSingleLineText
import com.kpstv.vpn.ui.components.HeaderButton
import com.kpstv.vpn.ui.components.ThemeButton
import com.kpstv.vpn.ui.helpers.AppPackage
import com.kpstv.vpn.ui.helpers.Settings
import com.kpstv.vpn.ui.screens.NavigationRoute
import com.kpstv.vpn.ui.theme.CommonPreviewTheme
import com.kpstv.vpn.ui.theme.dotColor
import com.kpstv.vpn.ui.viewmodels.AppSheetViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.parcelize.Parcelize
import java.util.*
import kotlin.math.abs

@Composable
fun AppsDialogMain(onDisallowedAppListChanged: () -> Unit) {
  if (LocalInspectionMode.current) return
  AppsDialog(onDisallowedAppListChanged = onDisallowedAppListChanged)
}

@Parcelize
object AppsDialog : DialogRoute

@Parcelize
private enum class DialogMode : Parcelable {
  Normal, Search
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AppsDialog(
  viewModel: AppSheetViewModel = viewModel(),
  onDisallowedAppListChanged: () -> Unit
) {
  val controller = findController(key = NavigationRoute.key)
  val context = LocalContext.current

  val dialogMode = rememberSaveable { mutableStateOf(DialogMode.Normal) }

  val searchText = rememberSaveable { mutableStateOf("") }

  controller.CreateDialog(
    key = AppsDialog::class,
    dialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    handleOnDismissRequest = request@{
      if (dialogMode.value == DialogMode.Search) {
        dialogMode.value = DialogMode.Normal
        searchText.value = ""
        return@request true
      }
      return@request false
    }
  ) {

    val disallowedAppPackages = remember { mutableStateListOf<String>() }
    val disallowedAppPackagesSnapshot = arrayListOf<String>()

    LaunchedEffect(Unit) {
      Settings.DisallowedVpnApps.get().collect { packages ->
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
        itemsIndexed(items = packages.value.filter {
          it.packageName.lowercase(Locale.ROOT).contains(searchText.value) ||
            it.name.lowercase(Locale.ROOT).contains(searchText.value)
        }) { index: Int, item: AppPackage ->

          if (index == 0) {
            key(index) {
              Spacer(modifier = Modifier.height(75.dp))
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
        searchText = searchText.value,
        onSearchTextChange = { searchText.value = it },
        onSelectAll = {
          disallowedAppPackages.clear()
        },
        onDeselectAll = {
          disallowedAppPackages.addAll(packages.value.map { it.packageName })
        },
        currentDialogMode = dialogMode.value,
        changeToSearchMode = { dialogMode.value = DialogMode.Search },
        changeToNormalMode = { dialogMode.value = DialogMode.Normal }
      )

      Footer(
        modifier = Modifier.align(Alignment.BottomCenter),
        onSaveClick = {
          if (disallowedAppPackagesSnapshot != disallowedAppPackages) {
            Settings.DisallowedVpnApps.set(disallowedAppPackages.toSet())
            onDisallowedAppListChanged()
          }
          controller.closeDialog(AppsDialog::class)
        }
      )

    }
  }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalComposeUiApi::class)
@Composable
private fun Header(
  filterVpnNumber: Int,
  searchText: String,
  onSearchTextChange: (String) -> Unit,
  onSelectAll: () -> Unit,
  onDeselectAll: () -> Unit,
  currentDialogMode: DialogMode = DialogMode.Normal,
  changeToSearchMode: () -> Unit,
  changeToNormalMode: () -> Unit
) {
  Column(modifier = Modifier.background(color = MaterialTheme.colors.background.copy(alpha = 0.93f))) {
    Spacer(modifier = Modifier.height(12.dp))

    Row(
      modifier = Modifier
        .padding(horizontal = 15.dp)
        .height(50.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      AnimatedContent(
        targetState = currentDialogMode,
        transitionSpec = {
          val multiplier = if (initialState == DialogMode.Search) -1 else 1
          ContentTransform(
            slideInHorizontally(initialOffsetX = { it * multiplier }) + fadeIn(),
            slideOutHorizontally(targetOffsetX = { -it * multiplier }) + fadeOut()
          )
        }
      ) { state ->
        when (state) {
          DialogMode.Normal -> {
            Row {
              Column(modifier = Modifier.padding(horizontal = 5.dp)) {
                Text(
                  text = stringResource(R.string.filter_apps),
                  style = MaterialTheme.typography.h4.copy(fontSize = 20.sp)
                )
                Spacer(modifier = Modifier.height(3.dp))
                AutoSizeSingleLineText(
                  text = stringResource(R.string.enable_vpn, filterVpnNumber),
                  style = MaterialTheme.typography.subtitle2,
                  color = MaterialTheme.colors.onSecondary
                )
              }
              Spacer(modifier = Modifier.weight(1f))
              HeaderButton(
                icon = R.drawable.ic_select_all,
                contentDescription = "select all",
                tooltip = stringResource(R.string.apps_dialog_select_all),
                onClick = { onSelectAll() }
              )
              HeaderButton(
                icon = R.drawable.ic_deselect_all,
                contentDescription = "deselect all",
                tooltip = stringResource(R.string.apps_dialog_deselect_all),
                onClick = { onDeselectAll() }
              )
              HeaderButton(
                icon = R.drawable.ic_search,
                contentDescription = "search",
                tooltip = stringResource(R.string.apps_dialog_search),
                onClick = { changeToSearchMode() }
              )
            }
          }
          DialogMode.Search -> {
            val keyboardController = LocalSoftwareKeyboardController.current
            val requester = FocusRequester()

            val colors = TextFieldDefaults.outlinedTextFieldColors()

            Row(modifier = Modifier.padding(end = 10.dp)) {
              HeaderButton(
                icon = R.drawable.ic_baseline_arrow_back_24,
                contentDescription = "back",
                onClick = {
                  changeToNormalMode()
                  onSearchTextChange("") // empty to reset
                }
              )
              Spacer(modifier = Modifier.width(10.dp))
              BasicTextField(
                modifier = Modifier
                  .fillMaxWidth()
                  .focusRequester(requester),
                singleLine = true,
                value = searchText,
                onValueChange = onSearchTextChange,
                textStyle = MaterialTheme.typography.h4.copy(color = MaterialTheme.colors.secondary, fontSize = 17.sp),
                cursorBrush = SolidColor(colors.cursorColor(false).value),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                  onDone = { keyboardController?.hide() }
                ),
                decorationBox = { innerTextField ->
                  Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                    innerTextField()
                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = MaterialTheme.colors.primary, thickness = 2.dp)
                  }
                }
              )

              LaunchedEffect(Unit) {
                requester.requestFocus()
              }
            }
          }
        }

      }
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
private fun PreviewHeaderNormal() {
  CommonPreviewTheme {
    Header(
      filterVpnNumber = 10,
      onSelectAll = { },
      onDeselectAll = { },
      searchText = "",
      onSearchTextChange = {},
      currentDialogMode = DialogMode.Normal,
      changeToNormalMode = {},
      changeToSearchMode = {}
    )
  }
}

@Preview
@Composable
private fun PreviewHeaderSearch() {
  CommonPreviewTheme {
    Header(
      filterVpnNumber = 10,
      onSelectAll = { },
      onDeselectAll = { },
      searchText = "",
      onSearchTextChange = {},
      currentDialogMode = DialogMode.Search,
      changeToNormalMode = {},
      changeToSearchMode = {}
    )
  }
}

@Preview
@Composable
private fun PreviewFooter() {
  CommonPreviewTheme {
    Footer(onSaveClick = {})
  }
}

@Preview
@Composable
private fun PreviewAppsItem() {
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
