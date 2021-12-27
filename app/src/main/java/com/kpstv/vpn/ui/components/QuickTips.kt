package com.kpstv.vpn.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kpstv.vpn.ui.theme.CommonPreviewTheme
import com.kpstv.vpn.ui.theme.dotColor
import java.util.*

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun QuickTip(
  message: String,
  visible: Boolean,
  modifier: Modifier = Modifier,
  cardColor: Color = dotColor,
  textColor: Color = MaterialTheme.colors.background,
  button: @Composable () -> Unit
) {
  AnimatedVisibility(
    visible = visible,
    enter = fadeIn() + expandVertically(),
    exit = fadeOut() + shrinkVertically()
  ) {
    Card(
      backgroundColor = cardColor,
      shape = RoundedCornerShape(5.dp),
      elevation = 3.dp
    ) {
      Column(modifier = modifier.padding(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            modifier = Modifier.weight(1f),
            text = message,
            style = MaterialTheme.typography.h5.copy(fontSize = 15.sp),
            color = textColor
          )
          Spacer(modifier = Modifier.width(10.dp))
          button()
        }
      }
    }
  }
}

@Composable
fun QuickTipDefaultButton(buttonText: String, buttonOnClick: () -> Unit) {
  ThemeButton(
    onClick = buttonOnClick,
    text = buttonText.uppercase(Locale.ROOT),
    fontSize = 12.sp
  )
}

@Composable
@Preview
fun PreviewQuickTip() {
  CommonPreviewTheme {
    Column(modifier = Modifier.padding(20.dp)) {
      var showTip by remember { mutableStateOf(false) }
      QuickTip(
        modifier = Modifier.fillMaxWidth(),
        message = "This is a small quick tip.",
        visible = !showTip,
        button = {
          ThemeButton(
            onClick = { showTip = true },
            text = "Learn more"
          )
        }
      )
    }
  }
}

@Composable
@Preview
fun PreviewQuickTipLongText() {
  CommonPreviewTheme {
    Column(modifier = Modifier.padding(20.dp)) {
      var showTip by remember { mutableStateOf(false) }
      QuickTip(
        modifier = Modifier.fillMaxWidth(),
        message = "This is a small quick tip & here are some extra text that should get this to next line.",
        visible = !showTip,
        button = {
          ThemeButton(
            onClick = { showTip = true },
            text =  "More"
          )
        }
      )
    }
  }
}
