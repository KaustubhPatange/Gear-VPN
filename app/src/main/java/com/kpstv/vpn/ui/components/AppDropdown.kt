package com.kpstv.vpn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties

@Composable
fun AppDropdownMenu(
  title: String,
  expandedState: MutableState<Boolean>,
  modifier: Modifier = Modifier,
  offset: DpOffset = DpOffset(0.dp, 0.dp),
  properties: PopupProperties = PopupProperties(focusable = true),
  content: @Composable ColumnScope.() -> Unit
) {

  DropdownMenu(
    expanded = expandedState.value,
    modifier = modifier
      .background(MaterialTheme.colors.primaryVariant),
    onDismissRequest = { expandedState.value = false },
    offset = offset,
    properties = properties,
    content = {
      Text(
        text = title,
        modifier = Modifier.padding(horizontal = 10.dp),
        style = MaterialTheme.typography.subtitle2,
      )
      Spacer(modifier = Modifier.height(10.dp))
      Divider()
      content()
    }
  )
}

@Composable
fun AppDropdownCheckBoxItem(text: String, checked: Boolean, onClick: () -> Unit) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = {
        onClick()
      })
      .padding(vertical = 10.dp, horizontal = 10.dp)
  ) {
    RadioButton(selected = checked, onClick = onClick)
    Text(
      text = text,
      modifier = Modifier
        .align(Alignment.CenterVertically)
        .padding(horizontal = 10.dp),
      color = MaterialTheme.colors.onSecondary,
      style = MaterialTheme.typography.button.copy(fontSize = 15.sp)
    )
  }
}

@Composable
fun AppDropdownIconItem(
  title: String,
  painter: Painter,
  contentDescription: String,
  onClick: () -> Unit
) {
  DropdownMenuItem(
    onClick = onClick
  ) {
    Icon(
      modifier = Modifier.size(24.dp),
      painter = painter,
      contentDescription = contentDescription
    )
    Spacer(modifier = Modifier.width(5.dp))
    Text(
      text = title,
      modifier = Modifier
        .align(Alignment.CenterVertically)
        .padding(horizontal = 5.dp),
      maxLines = 1,
      softWrap = false,
      color = MaterialTheme.colors.onSecondary,
      style = MaterialTheme.typography.button.copy(fontSize = 16.sp)
    )
  }
}
