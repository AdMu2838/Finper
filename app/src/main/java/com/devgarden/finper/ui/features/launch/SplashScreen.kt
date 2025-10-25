package com.devgarden.finper.ui.features.launch

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.Canvas
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.rotate
import com.devgarden.finper.R
import kotlinx.coroutines.delay
import com.devgarden.finper.ui.theme.FinperTheme
import com.devgarden.finper.ui.theme.PrimaryGreen
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onTimeout: () -> Unit) {

    val logoScale = remember { Animatable(0.6f) }
    val textAlpha = remember { Animatable(0f) }

    val infinite = rememberInfiniteTransition()
    val rotation by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 3000, easing = LinearEasing), RepeatMode.Restart)
    )

    LaunchedEffect(Unit) {
        val scope = this
        scope.launch {
            logoScale.animateTo(targetValue = 1f, animationSpec = tween(durationMillis = 700))
            logoScale.animateTo(targetValue = 0.95f, animationSpec = tween(durationMillis = 150))
            logoScale.animateTo(targetValue = 1f, animationSpec = tween(durationMillis = 150))
        }
        scope.launch {
            delay(250)
            textAlpha.animateTo(targetValue = 1f, animationSpec = tween(durationMillis = 700))
        }
        delay(2000L)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryGreen),
        contentAlignment = Alignment.Center
    ) {
        // Anillo rotatorio en el fondo
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(240.dp).rotate(rotation)) {
                val stroke = size.minDimension * 0.06f
                drawArc(
                    color = Color.White.copy(alpha = 0.08f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = stroke)
                )
                // Un arco más visible que gira
                drawArc(
                    color = Color.White.copy(alpha = 0.14f),
                    startAngle = 0f,
                    sweepAngle = 90f,
                    useCenter = false,
                    style = Stroke(width = stroke)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Logo animado (scale)
                Image(
                    painter = painterResource(id = R.drawable.ic_logo_finper),
                    contentDescription = "Logo de FinPer",
                    modifier = Modifier
                        .size(120.dp)
                        .scale(logoScale.value)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Nombre de la aplicación con fade in
                Text(
                    text = "FinPer",
                    color = Color.White.copy(alpha = textAlpha.value),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    FinperTheme {
        SplashScreen(onTimeout = {})
    }
}