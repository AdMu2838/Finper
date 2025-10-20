package com.devgarden.finper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.devgarden.finper.ui.theme.FinperTheme

class ExpenseBarActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FinperTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ExpenseBarScreen()
                }
            }
        }
    }
}

@Composable
fun ExpenseBarScreen() {
    var baseAmount by remember { mutableStateOf(1000f) }
    var expense by remember { mutableStateOf(0f) }
    val progress = (expense / baseAmount).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 700),
        label = "progress"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Dinero base: $baseAmount", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Gasto actual: $expense", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(24.dp))
        SemicircularProgressBar(progress = animatedProgress)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = {
            if (expense + 100f <= baseAmount) expense += 100f
        }) {
            Text("Agregar gasto (+100)")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            expense = 0f
        }) {
            Text("Reiniciar gastos")
        }
    }
}

@Composable
fun SemicircularProgressBar(progress: Float) {
    Box(
        modifier = Modifier.size(220.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(200.dp)) {
            val strokeWidth = 24f
            val diameter = size.width
            val arcRect = Size(diameter, diameter)
            // Fondo semicircular
            drawArc(
                color = Color.LightGray,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            // Progreso semicircular
            drawArc(
                color = Color(0xFF1976D2),
                startAngle = 180f,
                sweepAngle = 180f * progress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

