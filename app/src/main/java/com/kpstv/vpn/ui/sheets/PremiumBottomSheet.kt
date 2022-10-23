package com.kpstv.vpn.ui.sheets

import androidx.annotation.RawRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.google.accompanist.insets.navigationBarsPadding
import com.kpstv.vpn.R
import com.kpstv.vpn.ui.components.BottomSheetState
import com.kpstv.vpn.ui.components.ThemeButton
import com.kpstv.vpn.ui.theme.CommonPreviewTheme
import com.kpstv.vpn.ui.theme.Dimen.dp100
import com.kpstv.vpn.ui.theme.Dimen.sp18

@Composable
fun PremiumBottomSheet(
  premiumBottomSheet: BottomSheetState,
  isPremiumUnlocked: Boolean,
  onPremiumClick: () -> Unit,
) {
  BaseBottomSheet(bottomSheetState = premiumBottomSheet) {
    if (!isPremiumUnlocked) {
      CommonSheet(
        lottieRes = R.raw.premium_gold,
        text = stringResource(R.string.premium_text),
        buttonText = stringResource(R.string.premium_pay),
        onButtonClick = onPremiumClick,
      )
    } else {
      CommonSheet(
        lottieRes = R.raw.heart_with_particles,
        text = stringResource(R.string.premium_complete_text),
        buttonText = stringResource(R.string.premium_complete_button),
        onButtonClick = { premiumBottomSheet.hide() },
      )
    }
  }
}

@Composable
private fun CommonSheet(
  @RawRes lottieRes: Int,
  text: String,
  buttonText: String,
  onButtonClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .padding(horizontal = 5.dp)
      .fillMaxWidth()
      .wrapContentHeight()
      .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
      .background(MaterialTheme.colors.background)
      .padding(10.dp)
  ) {
    Column {
      Row {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieRes))
        val progress by animateLottieCompositionAsState(
          composition = composition,
          iterations = LottieConstants.IterateForever
        )
        LottieAnimation(
          modifier = Modifier.size(dp100), // it needs fixed size otherwise it fills the entire screen, [cc](github.com/airbnb/lottie-android/issues/1866)
          composition = composition,
          progress = progress
        )
        Text(
          modifier = Modifier.align(Alignment.CenterVertically),
          text = text,
          style = MaterialTheme.typography.h4.copy(fontSize = sp18)
        )
        Spacer(modifier = Modifier.width(20.dp))
      }
      Spacer(modifier = Modifier.height(20.dp))
      ThemeButton(
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp),
        onClick = onButtonClick,
        text = buttonText
      )
      Spacer(modifier = Modifier.navigationBarsPadding())
    }
  }
}

@Preview
@Composable
fun PreviewPremiumBottomSheet() {
  CommonPreviewTheme {
    CommonSheet(
      lottieRes = R.raw.premium_gold,
      text = stringResource(R.string.premium_text),
      buttonText = stringResource(R.string.premium_pay),
      onButtonClick = { },
    )
  }
}