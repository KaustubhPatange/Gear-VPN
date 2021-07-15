package com.kpstv.vpn.ui.components

import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.google.accompanist.insets.navigationBarsPadding
import com.kpstv.vpn.R

@Composable
fun PremiumBottomSheet(
  premiumBottomSheet: MutableState<BottomSheetState>,
  suppressBackPress: (Boolean) -> Unit
) {
  BottomSheet(bottomSheetState = premiumBottomSheet) {
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
          val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.premium_gold))
          val progress by animateLottieCompositionAsState(
            composition = composition,
            iterations = LottieConstants.IterateForever
          )
          LottieAnimation(
            modifier = Modifier.width(100.dp),
            composition = composition,
            progress = progress
          )
          Text(
            modifier = Modifier.align(Alignment.CenterVertically),
            text = stringResource(R.string.premium_text),
            style = MaterialTheme.typography.h4.copy(fontSize = 18.sp)
          )
          Spacer(modifier = Modifier.width(20.dp))
        }
        Spacer(modifier = Modifier.height(20.dp))
        ThemeButton(
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
          onClick = { /*TODO: Add premium*/ },
          text = stringResource(R.string.premium_pay)
        )
        Spacer(modifier = Modifier.navigationBarsPadding())
      }
    }

    val backpress = remember {
      object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
          premiumBottomSheet.value = BottomSheetState.Collapsed
        }
      }
    }

    LaunchedEffect(premiumBottomSheet.value) {
      suppressBackPress.invoke(premiumBottomSheet.value == BottomSheetState.Expanded)
      backpress.isEnabled = premiumBottomSheet.value == BottomSheetState.Expanded
    }

    val dispatcher = checkNotNull(LocalOnBackPressedDispatcherOwner.current).onBackPressedDispatcher
    DisposableEffect(Unit) {
      dispatcher.addCallback(backpress)
      onDispose {
        backpress.remove()
      }
    }
  }
}