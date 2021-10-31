package com.kpstv.vpn.ui.dialogs.content

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter

@OptIn(ExperimentalCoilApi::class)
@Composable
fun FeatureContent(name: String, @DrawableRes gif: Int, description: String) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(20.dp)
  ) {
    Text(
      text = name,
      style = MaterialTheme.typography.h5.copy(fontSize = 22.sp)
    )
    Spacer(modifier = Modifier.height(30.dp))
    Image(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(10.dp))
        .height(280.dp)
        .border(
          width = 1.dp,
          color = MaterialTheme.colors.secondary,
          shape = RoundedCornerShape(10.dp)
        ),
      contentScale = ContentScale.Crop,
      painter = rememberImagePainter(gif),
      contentDescription = "feature-demo"
    )
    Spacer(modifier = Modifier.height(30.dp))
    Text(
      text = description,
      color = MaterialTheme.colors.secondary,
      style = MaterialTheme.typography.h5
    )
  }
}