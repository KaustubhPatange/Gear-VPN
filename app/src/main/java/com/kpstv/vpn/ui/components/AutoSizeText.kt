package com.kpstv.vpn.ui.components

import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun AutoSizeSingleLineText(
  text: String,
  modifier: Modifier = Modifier,
  style: TextStyle = LocalTextStyle.current,
  color: Color = Color.Unspecified,
  overflow: TextOverflow = TextOverflow.Clip,
  // Add more parameters according to need
) {
  AutoSizeSingleLineText(
    text = AnnotatedString(text),
    modifier = modifier,
    style = style,
    color = color,
    overflow = overflow
  )
}

@Composable
fun AutoSizeSingleLineText(
  text: AnnotatedString,
  modifier: Modifier = Modifier,
  style: TextStyle = LocalTextStyle.current,
  color: Color = Color.Unspecified,
  overflow: TextOverflow = TextOverflow.Clip,
  // Add more parameters according to need
) {
  var textStyle by remember { mutableStateOf(style) }
  var readyToDraw by remember { mutableStateOf(false) }
  Text(
    text = text,
    color = color,
    style = textStyle,
    maxLines = 1,
    softWrap = false,
    overflow = overflow,
    modifier = modifier.drawWithContent {
      if (readyToDraw) drawContent()
    },
    onTextLayout = { textLayoutResult ->
      if (textLayoutResult.didOverflowWidth) {
        textStyle = textStyle.copy(fontSize = textStyle.fontSize * 0.9)
      } else {
        readyToDraw = true
      }
    }
  )
}