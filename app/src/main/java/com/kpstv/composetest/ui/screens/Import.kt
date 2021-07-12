package com.kpstv.composetest.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toFile
import com.kpstv.composetest.R
import com.kpstv.composetest.data.models.LocalConfiguration
import com.kpstv.composetest.extensions.utils.AppUtils.asPassword
import com.kpstv.composetest.ui.components.AnimatedSwipeDismiss
import com.kpstv.composetest.ui.components.Header
import com.kpstv.composetest.ui.components.ThemeButton
import com.kpstv.composetest.ui.theme.CommonPreviewTheme
import com.kpstv.composetest.ui.theme.dotColor
import androidx.compose.ui.platform.LocalContext
import es.dmoral.toasty.Toasty
import java.io.FileNotFoundException
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kpstv.composetest.data.models.asVpnConfiguration
import com.kpstv.composetest.ui.viewmodels.ImportViewModel
import com.kpstv.composetest.ui.viewmodels.VpnViewModel
import com.kpstv.navigation.compose.Fade
import com.kpstv.navigation.compose.findController

@Composable
fun ImportScreen(
  importViewModel: ImportViewModel = viewModel(),
  vpnViewModel: VpnViewModel = viewModel(),
  onDelete: (LocalConfiguration) -> Unit = {},
  goBack: () -> Unit = {}
) {
  val controller = findController(key = NavigationRoute.key)

  val localConfigurations = importViewModel.getConfigs.collectAsState(initial = emptyList())

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    LazyColumn {
      itemsIndexed(localConfigurations.value) { index, item ->
        if (index == 0) {
          Spacer(modifier = Modifier.height(100.dp))
        }
        ProfileItem(
          item = item,
          onSwipe = onDelete
        )
      }
    }

    Column {
      Header(title = stringResource(R.string.import_config), goBack)
      Spacer(modifier = Modifier.height(10.dp))
      Profile(
        connect = { config, toSave ->
          if (toSave) importViewModel.addConfig(config)
          vpnViewModel.changeServer(config.asVpnConfiguration())

          controller.navigateTo(NavigationRoute.Main()) {
            popUpTo(NavigationRoute.Main()) {
              inclusive = false
            }
            withAnimation {
              target = Fade
              current = Fade
            }
          }
        }
      )
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = stringResource(R.string.vpn_local_configurations),
        style = MaterialTheme.typography.h4.copy(fontSize = 20.sp),
        color = MaterialTheme.colors.onSecondary
      )
    }
  }
}

@Composable
private fun Profile(connect: (config: LocalConfiguration, save: Boolean) -> Unit) {
  val fileUri = remember { mutableStateOf(Uri.EMPTY) }
  val fileLocation = derivedStateOf {
    if (fileUri.value.scheme == "file") {
      fileUri.value.toFile().absolutePath
    } else null
  }
  val context = LocalContext.current
  val userName = rememberSaveable { mutableStateOf("") }

  val password = rememberSaveable { mutableStateOf("") }

  val profileName = rememberSaveable { mutableStateOf("") }

  val saveProfile = remember { mutableStateOf(true) }

  val openDocumentResult =
    rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri ->
      fileUri.value = uri
      if (uri.scheme == "file") {
        uri.toFile().absolutePath
      }
    }

  ProfileColumn(
    fileLocation = fileLocation.value,
    onFileChoose = { openDocumentResult.launch(arrayOf("text/plain")) },
    userName = userName.value,
    onUserNameChanged = { userName.value = it },
    password = password.value,
    onPasswordChanged = { password.value = it },
    profileName = profileName.value,
    onProfileNameChanged = { profileName.value = it },
    saveProfile = saveProfile.value,
    onSaveProfileChanged = { saveProfile.value = it },
    onConnectToProfile = {
      try {
        context.contentResolver.openInputStream(fileUri.value)?.let { stream ->
          val config = stream.bufferedReader().readText()
          // TODO: Add more ways to verify if this is a correct configuration file.
          stream.close()
          connect.invoke(
            /*config*/ LocalConfiguration(
              profileName = profileName.value,
              userName = userName.value,
              password = password.value,
              config = config
            ),
            /*toSave*/ saveProfile.value
          )
        }
      } catch (e: FileNotFoundException) {
        Toasty.error(context, context.getString(R.string.invalid_config)).show()
      }
    }
  )
}

@Composable
private fun ProfileColumn(
  fileLocation: String?,
  onFileChoose: () -> Unit = {},
  userName: String = "",
  onUserNameChanged: (String) -> Unit = {},
  password: String = "",
  onPasswordChanged: (String) -> Unit = {},
  profileName: String = "",
  onProfileNameChanged: (String) -> Unit = {},
  saveProfile: Boolean = true,
  onSaveProfileChanged: (Boolean) -> Unit = {},
  onConnectToProfile: () -> Unit = {}
) {
  val passwordVisibility = remember { mutableStateOf(false) }

  Column(modifier = Modifier.padding(15.dp)) {
    // Choose a file
    Row {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = stringResource(R.string.profile_select_file),
          style = MaterialTheme.typography.h5
        )
        Text(
          text = stringResource(
            R.string.profile_path,
            fileLocation ?: stringResource(R.string.profile_path_invalid)
          ),
          modifier = Modifier.padding(top = 2.dp),
          style = MaterialTheme.typography.h4.copy(fontSize = 13.sp),
          color = MaterialTheme.colors.onSecondary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
      Spacer(modifier = Modifier.width(10.dp))
      ThemeButton(
        onClick = onFileChoose,
        modifier = Modifier.align(Alignment.CenterVertically),
        text = stringResource(R.string.choose)
      )
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Username
    TextField(
      modifier = Modifier.fillMaxWidth(),
      value = userName,
      onValueChange = onUserNameChanged,
      singleLine = true,
      placeholder = {
        Text(text = stringResource(R.string.profile_username))
      }
    )

    // Password
    TextField(
      modifier = Modifier.fillMaxWidth(),
      value = password,
      onValueChange = onPasswordChanged,
      singleLine = true,
      visualTransformation = if (passwordVisibility.value) VisualTransformation.None else PasswordVisualTransformation(),
      shape = MaterialTheme.shapes.small.copy(ZeroCornerSize),
      placeholder = {
        Text(text = stringResource(R.string.profile_password))
      },
      trailingIcon = {
        IconButton(onClick = { passwordVisibility.value = !passwordVisibility.value }) {
          Icon(
            painter = painterResource(
              if (passwordVisibility.value) R.drawable.ic_baseline_visibility_off_24
              else R.drawable.ic_baseline_visibility_24
            ), contentDescription = "password visibility"
          )
        }
      }
    )

    Spacer(modifier = Modifier.height(20.dp))

    // Profile name
    TextField(
      modifier = Modifier.fillMaxWidth(),
      value = profileName,
      onValueChange = onProfileNameChanged,
      singleLine = true,
      placeholder = {
        Text(text = stringResource(R.string.profile_name))
      },
      colors = TextFieldDefaults.textFieldColors(
        backgroundColor = Color.Transparent
      )
    )

    Spacer(modifier = Modifier.height(25.dp))

    Row(
      modifier = Modifier.clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() },
        onClick = { onSaveProfileChanged.invoke(!saveProfile) }
      )) {
      Checkbox(
        modifier = Modifier.align(Alignment.CenterVertically),
        checked = saveProfile,
        onCheckedChange = onSaveProfileChanged
      )
      Spacer(modifier = Modifier.width(10.dp))
      Text(
        text = stringResource(R.string.save_profile),
        style = MaterialTheme.typography.h2.copy(fontSize = 16.sp)
      )
    }

    Spacer(modifier = Modifier.height(20.dp))

    ThemeButton(
      onClick = onConnectToProfile,
      modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)
        .clip(RoundedCornerShape(10.dp)),
      text = stringResource(R.string.profile_connect)
    )
  }
}

@Composable
private fun ProfileItem(item: LocalConfiguration, onSwipe: (LocalConfiguration) -> Unit = {}) {
  AnimatedSwipeDismiss(
    item = item,
    background = { isDismissed ->
      /** define your background delete view here
       * possibly:
      Box(
      modifier = Modifier.fillMaxSize(),
      backgroundColor = Color.Red,
      paddingStart = 20.dp,
      paddingEnd = 20.dp,
      gravity = ContentGravity.CenterEnd
      ) {
      val alpha = animate( if (isDismissed) 0f else 1f)
      Icon(Icons.Filled.Delete, tint = Color.White.copy(alpha = alpha))
      }

      using isDismissed to control alpha of the icon or content in the box
       */
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Red)
          .padding(horizontal = 20.dp)
          .clip(RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.CenterEnd,
      ) {
        val alpha by animateFloatAsState(if (isDismissed) 0f else 1f)
        Icon(
          painter = painterResource(R.drawable.ic_delete_bin),
          tint = Color.White.copy(alpha = alpha),
          contentDescription = null
        )
      }
    },
    content = {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .wrapContentHeight()
          .border(
            width = 1.5.dp,
            color = dotColor.copy(alpha = 0.7f),
            shape = RoundedCornerShape(10.dp)
          )
          .background(MaterialTheme.colors.background)
          .padding(13.dp)
      ) {
        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.Center
        ) {
          Text(
            text = item.profileName,
            style = MaterialTheme.typography.h4.copy(fontSize = 18.sp),
            overflow = TextOverflow.Ellipsis,

            maxLines = 1
          )
          Spacer(modifier = Modifier.height(1.dp))
          Text(
            text = stringResource(
              R.string.profile_item_subtitle,
              item.userName,
              item.password.asPassword()
            ),
            style = MaterialTheme.typography.subtitle2,
            color = MaterialTheme.colors.onSurface,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
          )
        }
        /*IconButton(onClick = {  }) {
          Icon(painter = , contentDescription = )
        }*/
      }
    },
    onDismiss = { onSwipe(it) }
  )
}

@Preview(showBackground = true)
@Composable
fun PreviewProfileScreen() {
  CommonPreviewTheme {
    ProfileColumn(
      fileLocation = null,
    )
  }
}

@Preview(showBackground = true)
@Composable
fun PreviewProfileItem() {
  CommonPreviewTheme {
    ProfileItem(
      item = LocalConfiguration(
        profileName = "Test Profile",
        userName = "vpn",
        password = "vpn",
        config = ""
      )
    )
  }
}

