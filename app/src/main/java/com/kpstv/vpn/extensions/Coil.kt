package com.kpstv.vpn.extensions

import android.os.Build.VERSION.SDK_INT
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.compose.LocalImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder

@Composable
fun CoilCustomImageLoader(content: @Composable () -> Unit) {
  val context = LocalContext.current
  val imageLoader = ImageLoader.invoke(context).newBuilder()
    .componentRegistry {
      if (SDK_INT >= 28) {
        add(ImageDecoderDecoder(context))
      } else {
        add(GifDecoder())
      }
      add(SvgDecoder(context))
    }.build()
  CompositionLocalProvider(LocalImageLoader provides imageLoader) {
    content()
  }
}