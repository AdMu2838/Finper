package com.devgarden.finper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.devgarden.finper.ui.theme.FinperTheme

class CircleAnimationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FinperTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CircleAnimationScreen()
                }
            }
        }
    }
}

@Composable
fun CircleAnimationScreen() {
    var expanded by remember { mutableStateOf(false) }
    val circleSize by animateDpAsState(
        targetValue = if (expanded) 200.dp else 80.dp,
        animationSpec = tween(durationMillis = 10000),
        label = "circleSize"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(circleSize)
                .clip(CircleShape)
                .background(Color(0xFF1976D2))
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { expanded = !expanded }) {
            Text(if (expanded) "Reducir círculo" else "Expandir círculo")
        }
        Spacer(modifier = Modifier.height(16.dp))
        val context = LocalContext.current
        Button(onClick = {
            val intent = android.content.Intent(context, ExpenseBarActivity::class.java)
            context.startActivity(intent)
        }) {
            Text("Ir a barra de gastos")
        }
    }
}
