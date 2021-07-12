package com.kpstv.composetest.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.kpstv.composetest.extensions.utils.AppUtils.asPassword
import com.kpstv.composetest.ui.components.Header
import com.kpstv.composetest.ui.components.ThemeButton
import com.kpstv.composetest.ui.theme.CommonPreviewTheme
import com.kpstv.composetest.ui.theme.dotColor
import com.kpstv.composetest.ui.theme.goldenYellow

@Composable
fun ImportScreen(
  goBack: () -> Unit = {}
) {
  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    LazyColumn {
      itemsIndexed(listOf("")) { index, item ->
        if (index == 0) {
          Spacer(modifier = Modifier.height(100.dp))
        }
      }
    }

    Column {
      Header(title = stringResource(R.string.import_config))
      Spacer(modifier = Modifier.height(10.dp))
      Profile()
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = "Local configurations",
        style = MaterialTheme.typography.h4.copy(fontSize = 20.sp),
        color = MaterialTheme.colors.onSecondary
      )
    }
  }
}

@Composable
private fun Profile() {
  val fileUri = remember { mutableStateOf(Uri.EMPTY) }
  val fileLocation = derivedStateOf {
    if (fileUri.value.scheme == "file") {
      fileUri.value.toFile().absolutePath
    } else null
  }

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
    onSubmitClicked = {
      /*TODO*/
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
  onSubmitClicked: () -> Unit = {}
) {
  val passwordVisibility = remember { mutableStateOf(false) }

  Column(modifier = Modifier.padding(15.dp)) {
    // Choose a file
    Row() {
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
      onClick = onSubmitClicked,
      modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)
        .clip(RoundedCornerShape(10.dp)),
      text = stringResource(R.string.profile_add)
    )
  }
}

@Composable
private fun ProfileItem(profileName: String, userName: String, password: String) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .wrapContentHeight()
      .border(
        width = 1.5.dp,
        color = dotColor.copy(alpha = 0.7f),
        shape = RoundedCornerShape(10.dp)
      )
      .padding(13.dp),
    verticalArrangement = Arrangement.Center
  ) {
    Text(
      text = profileName,
      style = MaterialTheme.typography.h4.copy(fontSize = 18.sp),
      overflow = TextOverflow.Ellipsis,

      maxLines = 1
    )
    Spacer(modifier = Modifier.height(1.dp))
    Text(
      text = stringResource(
        R.string.profile_item_subtitle,
        userName,
        password.asPassword()
      ),
      style = MaterialTheme.typography.subtitle2,
      color = MaterialTheme.colors.onSurface,
      overflow = TextOverflow.Ellipsis,
      maxLines = 1
    )
  }
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
      profileName = "Test Profile",
      userName = "vpn",
      password = "vpn"
    )
  }
}

