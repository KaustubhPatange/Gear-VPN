package com.kpstv.vpn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TooltipText(text: String, modifier: Modifier = Modifier) {
  Text(
    modifier = modifier
      .clip(RoundedCornerShape(3.dp))
      .background(MaterialTheme.colors.primary.copy(alpha = 0.7f))
      .padding(vertical = 5.dp, horizontal = 10.dp),
    text = text,
    style = MaterialTheme.typography.h4.copy(fontSize = 14.sp),
    color = MaterialTheme.colors.secondary,
  )
}