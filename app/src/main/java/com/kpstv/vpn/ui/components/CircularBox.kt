@file:Suppress("TransitionPropertiesLabel", "UpdateTransitionLabel")

package com.kpstv.vpn.ui.components

import android.content.Context
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kpstv.vpn.ui.theme.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.kpstv.vpn.R

enum class ConnectivityStatus {
  NONE,
  CONNECTING,
  CONNECTED,
  DISCONNECT,
  RECONNECTING
}

private enum class CircularAnimateState {
  NOT_STARTED,
  SHRINK_IN,
  SHRINK_OUT,
  ROTATE,
  SHRINK_OUT_BIT // connected
}

private val diameter = 160.dp
private val dotSize = 3.dp
private val dashSize = 10.dp
private val arcThickness = 3.dp
private val circleDistance = 33.dp

private val boxHeight = diameter + circleDistance.times(2) + dashSize.times(2)

private fun getStatusAsText(context: Context, status: ConnectivityStatus): String {
  return when (status) {
    ConnectivityStatus.CONNECTING -> context.getString(R.string.status_connecting)
    ConnectivityStatus.CONNECTED -> context.getString(R.string.status_connected)
    ConnectivityStatus.RECONNECTING -> context.getString(R.string.status_reconnecting)
    else -> context.getString(R.string.status_connect)
  }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CircularBox(
  modifier: Modifier = Modifier,
  status: ConnectivityStatus,
) {
  val context = LocalContext.current

  val radiusPx = with(LocalDensity.current) { diameter.toPx() / 2 }
  val dotSizePx = with(LocalDensity.current) { dotSize.toPx() }
  val dashSizePx = with(LocalDensity.current) { dashSize.toPx() }
  val lineSizePx = with(LocalDensity.current) { arcThickness.toPx() }
  val distancePx = with(LocalDensity.current) { circleDistance.toPx() }
  val maxSweepAngle = remember { 65f }

  val circularState = remember { mutableStateOf(CircularAnimateState.NOT_STARTED) }

  val arcOffsetState = remember { mutableStateOf(0f) }
  val textState = remember { mutableStateOf(getStatusAsText(context, status)) }

  fun updateTextState() {
    textState.value = getStatusAsText(context, status)
  }

  val transition = updateTransition(circularState)

  val scaleInner by transition.animateFloat(
    transitionSpec = {
      if (targetState.value == CircularAnimateState.SHRINK_IN) {
        tween(durationMillis = 500)
      } else tween()
    }
  ) { state ->
    when (state.value) {
      CircularAnimateState.SHRINK_IN -> 0.8f
      CircularAnimateState.SHRINK_OUT -> 1f
      CircularAnimateState.SHRINK_OUT_BIT -> 1.06f
      else -> 1f
    }
  }

  val scaleOuter by transition.animateFloat(
    transitionSpec = {
      if (initialState.value == CircularAnimateState.SHRINK_IN) {
        tween(durationMillis = 500)
      } else tween()
    }
  ) { state ->
    when (state.value) {
      CircularAnimateState.SHRINK_IN -> 0.55f
      else -> 1f
    }
  }

  val infiniteTransition = rememberInfiniteTransition()
  val degreesInner by infiniteTransition.animateFloat(
    initialValue = 360f,
    targetValue = 0f,
    animationSpec = infiniteRepeatable(
      animation = tween(20000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    )
  )
  val degreesOuter = remember { Animatable(0f) }

  val sweepArc = remember { Animatable(0f) }
  val rotateArcYellow = remember { Animatable(0f) }
  val rotateArcCyan = remember { Animatable(0f) }

  val alphaArc = remember { Animatable(1f) }
  val alphaText = remember { Animatable(1f) }

  BoxWithConstraints(modifier = Modifier.height(boxHeight)) {
    Text(
      modifier = Modifier
        .align(Alignment.Center)
        .alpha(alphaText.value)
        .animateContentSize(),
      text = textState.value,
      style = MaterialTheme.typography.h5,
    )
    Canvas(modifier = modifier.fillMaxSize()) {
      val finalRadius = radiusPx + arcOffsetState.value
      val topLeft = Offset(
        x = size.width / 2f - finalRadius - distancePx - 10f,
        y = size.height / 2f - finalRadius - distancePx - 10f
      )
      val arcSize = Size(2 * (finalRadius + distancePx + 10f), 2 * (finalRadius + distancePx + 10f))

      withTransform({
        scale(scaleX = scaleInner, scaleY = scaleInner)
        rotate(
          degrees = when (circularState.value) {
            CircularAnimateState.ROTATE -> degreesInner
            else -> 360f
          }
        )
      }) {
        drawCircle(
          color = dotColor,
          radius = radiusPx,
          style = Stroke(
            width = dotSizePx,
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(
              floatArrayOf(0f, 30f)
            )
          )
        )
      }

      withTransform({
        scale(scaleX = scaleOuter, scaleY = scaleOuter)
        rotate(degrees = degreesOuter.value)
      }) {
        drawCircle(
          color = dotColor,
          radius = radiusPx + distancePx,
          style = Stroke(
            width = dashSizePx,
            pathEffect = PathEffect.dashPathEffect(
              floatArrayOf(5f, 26f)
            )
          )
        )
      }

      // yellow
      rotate(degrees = rotateArcYellow.value) {
        drawArc(
          color = if (arcOffsetState.value == 0f) goldenYellow else greenColor,
          startAngle = 0f,
          alpha = alphaArc.value,
          sweepAngle = sweepArc.value,
          useCenter = false,
          topLeft = topLeft,
          size = arcSize,
          style = Stroke(
            width = lineSizePx,
            cap = StrokeCap.Round
          )
        )
        drawArc(
          color = if (arcOffsetState.value == 0f) goldenYellow else greenColor,
          startAngle = 0f,
          alpha = if (alphaArc.value < 1f) 0f else 0.28f,
          sweepAngle = sweepArc.value,
          useCenter = false,
          topLeft = topLeft,
          size = arcSize,
          style = Stroke(
            width = lineSizePx + 10f,
            cap = StrokeCap.Round
          )
        )
        drawArc(
          color = if (arcOffsetState.value == 0f) goldenYellow else greenColor,
          startAngle = 0f,
          alpha = if (alphaArc.value < 1f) 0f else 0.23f,
          sweepAngle = sweepArc.value,
          useCenter = false,
          topLeft = topLeft,
          size = arcSize,
          style = Stroke(
            width = lineSizePx + 20f,
            cap = StrokeCap.Round
          )
        )
      }

      // cyan
      rotate(degrees = rotateArcCyan.value) {
        drawArc(
          color = if (arcOffsetState.value == 0f) cyan else greenColor,
          startAngle = 180f,
          alpha = alphaArc.value,
          sweepAngle = sweepArc.value,
          useCenter = false,
          topLeft = topLeft,
          size = arcSize,
          style = Stroke(
            width = lineSizePx,
            cap = StrokeCap.Round
          )
        )
        drawArc(
          color = if (arcOffsetState.value == 0f) cyan else greenColor,
          alpha = if (alphaArc.value < 1f) 0f else 0.28f,
          startAngle = 180f,
          sweepAngle = sweepArc.value,
          useCenter = false,
          topLeft = topLeft,
          size = arcSize,
          style = Stroke(
            width = lineSizePx + 10f,
            cap = StrokeCap.Round
          )
        )
        drawArc(
          color = if (arcOffsetState.value == 0f) cyan else greenColor,
          alpha = if (alphaArc.value < 1f) 0f else 0.23f,
          startAngle = 180f,
          sweepAngle = sweepArc.value,
          useCenter = false,
          topLeft = topLeft,
          size = arcSize,
          style = Stroke(
            width = lineSizePx + 20f,
            cap = StrokeCap.Round
          )
        )
      }
    }
  }

  suspend fun reset() {
    circularState.value = CircularAnimateState.NOT_STARTED
    updateTextState()
    degreesOuter.snapTo(0f)
    sweepArc.snapTo(0f)
    rotateArcYellow.snapTo(0f)
    rotateArcCyan.snapTo(0f)
    alphaArc.snapTo(1f)
    alphaText.snapTo(1f)
    arcOffsetState.value = 0f
  }

  LaunchedEffect(status) {

    suspend fun onConnecting() {
      circularState.value = CircularAnimateState.ROTATE
      updateTextState()

      // text blink effect
      launch {
        alphaText.animateTo(
          targetValue = 0.35f,
          animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
          )
        )
      }

      // arc scale
      launch {
        sweepArc.animateTo(
          targetValue = maxSweepAngle,
          animationSpec = tween(durationMillis = 700, easing = LinearEasing)
        )
      }
      launch {
        rotateArcYellow.animateTo(
          targetValue = 360f,
          animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing)
          )
        )
      }
      launch {
        rotateArcCyan.animateTo(
          targetValue = 360f,
          animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
          )
        )
      }

      // outer circle rotation
      degreesOuter.animateTo(
        targetValue = 360f,
        animationSpec = tween(
          5000, easing = CubicBezierEasing(
            1f, 0f, 1f, 1.01f
          )
        )
      )

      degreesOuter.snapTo(0f)
      degreesOuter.animateTo(
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
          animation = tween(1000),
          repeatMode = RepeatMode.Reverse
        )
      )
    }

    suspend fun connecting() {
      // ensure the defaults
      reset()

      circularState.value = CircularAnimateState.SHRINK_IN
      delay(600)
      circularState.value = CircularAnimateState.SHRINK_OUT
      delay(350)

      onConnecting()
    }

    when (status) {
      ConnectivityStatus.CONNECTING -> {
        connecting()
      }
      ConnectivityStatus.CONNECTED -> {
        sweepArc.snapTo(maxSweepAngle)

        launch {
          degreesOuter.snapTo(0f)
          degreesOuter.animateTo(
            targetValue = 360f,
            animationSpec = tween(
              5000, easing = CubicBezierEasing(
                0f, .32f, 1f, 1f
              )
            )
          )
          degreesOuter.snapTo(0f)
        }

        val arcYellow = async {
          val value = rotateArcYellow.value
          if (value > 270f) {
            rotateArcYellow.animateTo(540f, tween(durationMillis = 350, easing = LinearEasing))
            rotateArcYellow.snapTo(180f)
          }
          rotateArcYellow.animateTo(
            targetValue = 508f,
            animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
          )
        }
        val arcCyan = async {
          val value = rotateArcCyan.value
          if (value < 90f) {
            rotateArcCyan.animateTo(
              180f,
              tween(durationMillis = (90 - value.toInt()) * (350 / 70), easing = LinearEasing)
            )
          }
          rotateArcCyan.animateTo(
            targetValue = 511f, // 151f
            animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
          )
        }
        awaitAll(arcYellow, arcCyan)

        // blink effect
        alphaArc.animateTo(
          targetValue = 0f,
          animationSpec = repeatable(
            iterations = 4,
            animation = tween(90),
            repeatMode = RepeatMode.Reverse
          )
        )

        arcOffsetState.value = dashSizePx / 1.2f
        alphaArc.snapTo(1f)

        // now update text
        updateTextState()
        alphaText.snapTo(1f)

        circularState.value = CircularAnimateState.SHRINK_OUT_BIT
      }
      ConnectivityStatus.DISCONNECT -> {
        // ensure connected state
        circularState.value = CircularAnimateState.SHRINK_OUT_BIT
        alphaArc.snapTo(1f)
        alphaText.snapTo(1f)
        updateTextState()
        arcOffsetState.value = dashSizePx / 1.2f

        circularState.value = CircularAnimateState.SHRINK_IN
        delay(600)
        launch {
          sweepArc.animateTo(
            targetValue = 0f,
            animationSpec = tween(400)
          )
        }

        updateTextState()
        circularState.value = CircularAnimateState.SHRINK_OUT
        delay(350)
        reset()
      }
      ConnectivityStatus.RECONNECTING -> {
        val isConnectedState = circularState.value == CircularAnimateState.SHRINK_OUT_BIT
        val isConnectingState = circularState.value == CircularAnimateState.ROTATE
        if (isConnectedState || isConnectingState) {
          arcOffsetState.value = 0f
          alphaArc.snapTo(1f)
          alphaText.snapTo(1f)

          val arcYellow = async {
            rotateArcYellow.animateTo(
              targetValue = 0f,
              animationSpec = tween(1500, easing = LinearEasing)
            )
          }
          val arcCyan = async {
            rotateArcCyan.animateTo(
              targetValue = 0f,
              animationSpec = tween(1500, easing = LinearEasing)
            )
          }
          awaitAll(arcYellow, arcCyan)

          updateTextState()

          onConnecting()
        } else {
          connecting()
        }
      }
      ConnectivityStatus.NONE -> {
        reset()
      }
      /*ConnectivityStatus.ALREADY_CONNECTED -> {
        arcOffsetState.value = dashSizePx / 1.2f
        circularState.value = CircularAnimateState.SHRINK_OUT_BIT
        degreesOuter.snapTo(0f)
        rotateArcYellow.snapTo(508f)
        rotateArcCyan.snapTo(511f)
      }*/
      /* else -> {

       }*/
    }
  }
}

@Preview
@Composable
fun DefaultPreview() {
  CommonPreviewTheme {
    CircularBox(status = ConnectivityStatus.NONE)
  }
}

@Preview
@Composable
fun PlaygroundPreview() {
  val status = remember { mutableStateOf(ConnectivityStatus.NONE) }

  LaunchedEffect(status.value) {
    if (status.value == ConnectivityStatus.CONNECTING) {
      delay(5000)
      status.value = ConnectivityStatus.RECONNECTING
      delay(4000)
      status.value = ConnectivityStatus.CONNECTED
    }
  }

  CommonPreviewTheme {
    Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      CircularBox(status = status.value)
      Spacer(modifier = Modifier.height(30.dp))
      ThemeButton(
        onClick = {
          when (status.value) {
            ConnectivityStatus.NONE -> status.value = ConnectivityStatus.CONNECTING
            ConnectivityStatus.CONNECTED -> status.value = ConnectivityStatus.DISCONNECT
            else -> status.value = ConnectivityStatus.NONE
          }
        },
        text = if (status.value == ConnectivityStatus.DISCONNECT || status.value == ConnectivityStatus.NONE)
          "Connect" else "Disconnect"
      )
    }
  }
}
