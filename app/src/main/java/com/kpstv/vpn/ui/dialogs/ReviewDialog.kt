package com.kpstv.vpn.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.kpstv.navigation.compose.DialogRoute
import com.kpstv.navigation.compose.findNavController
import com.kpstv.vpn.R
import com.kpstv.vpn.extensions.clickableNoIndication
import com.kpstv.vpn.extensions.isDialogShowing
import com.kpstv.vpn.extensions.utils.AppUtils.launchUrl
import com.kpstv.vpn.ui.helpers.ReviewSettings
import com.kpstv.vpn.ui.screens.NavigationRoute
import com.kpstv.vpn.ui.theme.CommonPreviewTheme
import es.dmoral.toasty.Toasty
import kotlinx.parcelize.Parcelize

@Parcelize
object ReviewDialog : DialogRoute

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ReviewDialogScreen() {
  if (LocalInspectionMode.current) return

  val navController = findNavController(key = NavigationRoute.key)
  navController.CreateDialog(
    key = ReviewDialog::class,
    dialogProperties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    ReviewDialog()
  }

  LaunchedEffect(Unit) {
    if (ReviewSettings.canShowDialog() && !navController.isDialogShowing(ReviewDialog)) {
      navController.showDialog(ReviewDialog)
    }
  }
}

@Composable
private fun ReviewDialog() {
  val navController = findNavController(key = NavigationRoute.key)
  val context = LocalContext.current

  fun doNotShowAgain() = with(context) {
    ReviewSettings.doNotShowDialogAgain()
    Toasty.info(this, getString(R.string.rate_dialog_ignore)).show()
  }

  Column(
    modifier = Modifier.padding(40.dp),
    verticalArrangement = Arrangement.Center
  ) {
    ReviewMainContent(
      onReviewClick = { alreadyReviewed ->
        ReviewSettings.resetAll()
        if (alreadyReviewed) {
          doNotShowAgain()
        } else {
          context.launchUrl(context.getString(R.string.app_google_play))
        }
        navController.closeDialog(ReviewDialog::class)
      },
      onClose = { alreadyReviewed ->
        ReviewSettings.resetAll()
        if (alreadyReviewed) {
          doNotShowAgain()
        }
        navController.closeDialog(ReviewDialog::class)
      }
    )
  }
}

@Composable
private fun ReviewMainContent(
  onReviewClick: (alreadyReviewed: Boolean) -> Unit,
  onClose: (Boolean) -> Unit
) {
  Column(
    modifier = Modifier
      .wrapContentHeight()
      .clip(RoundedCornerShape(5.dp))
      .background(MaterialTheme.colors.background),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {

    val alreadyReview = remember { mutableStateOf(false) }

    Spacer(modifier = Modifier.height(20.dp))

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.start_zoom))
    val progress by animateLottieCompositionAsState(
      composition = composition,
      iterations = 1
    )
    LottieAnimation(
      modifier = Modifier.size(80.dp),
      composition = composition,
      progress = progress
    )
    Spacer(modifier = Modifier.height(20.dp))

    Text(
      text = stringResource(R.string.rate_dialog_title),
      style = MaterialTheme.typography.h5.copy(fontSize = 22.sp)
    )

    Spacer(modifier = Modifier.height(10.dp))

    Text(
      modifier = Modifier.padding(horizontal = 15.dp),
      text = stringResource(R.string.rate_dialog_text),
      textAlign = TextAlign.Center,
      style = MaterialTheme.typography.h5,
      color = MaterialTheme.colors.onSecondary,
    )

    Spacer(modifier = Modifier.height(20.dp))

    Row(
      modifier = Modifier.clickableNoIndication {
        alreadyReview.value = !alreadyReview.value
      },
      verticalAlignment = Alignment.CenterVertically
    ) {
      Checkbox(checked = alreadyReview.value, onCheckedChange = { alreadyReview.value = it })
      Spacer(modifier = Modifier.width(10.dp))
      Text(
        text = stringResource(R.string.rate_dialog_already_reviewed),
        style = MaterialTheme.typography.h5.copy(fontSize = 14.sp),
        color = MaterialTheme.colors.secondary
      )
    }

    Spacer(modifier = Modifier.height(15.dp))

    Divider()

    Spacer(modifier = Modifier.height(20.dp))

    Row {
      Button(
        onClick = { onClose(alreadyReview.value) },
        colors = ButtonDefaults.outlinedButtonColors(
          backgroundColor = MaterialTheme.colors.background
        ),
        border = BorderStroke(1.dp, MaterialTheme.colors.secondary)
      ) {
        Text(
          text = stringResource(R.string.rate_dialog_later),
          color = MaterialTheme.colors.secondary
        )
      }
      Spacer(modifier = Modifier.width(20.dp))
      Button(
        onClick = { onReviewClick(alreadyReview.value) },
        colors = ButtonDefaults.buttonColors()
      ) {
        Text(
          text = stringResource(R.string.rate_dialog_review),
          color = MaterialTheme.colors.onSecondary
        )
      }
    }

    Spacer(modifier = Modifier.height(20.dp))
  }
}


@Preview
@Composable
private fun PreviewReviewDialog() {
  CommonPreviewTheme {
    ReviewMainContent(
      onReviewClick = {},
      onClose = {}
    )
  }
}