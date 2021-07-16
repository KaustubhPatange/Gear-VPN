package com.kpstv.vpn.ui.activities

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.kpstv.vpn.R
import com.kpstv.vpn.extensions.utils.AppUtils.setEdgeToEdgeSystemUiFlags

class Splash : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setEdgeToEdgeSystemUiFlags()

    setContentView(R.layout.layout_splash)

    val imageView = findViewById<ImageView>(R.id.iv_splash)
    val avdSplash = AnimatedVectorDrawableCompat.create(this, R.drawable.avd_ic_logo)

    imageView.setImageDrawable(avdSplash)
    window.setBackgroundDrawable(
      ColorDrawable(
        ContextCompat.getColor(this, R.color.background)
      )
    )
    imageView.doOnLayout {
      avdSplash?.registerAnimationCallback(animationCallback)
      avdSplash?.start()
    }
  }

  private val animationCallback = object : Animatable2Compat.AnimationCallback() {
    override fun onAnimationEnd(drawable: Drawable?) {
      super.onAnimationEnd(drawable)
      val intent = Intent(this@Splash, Main::class.java)
      startActivity(intent)
      finish()
    }
  }
}