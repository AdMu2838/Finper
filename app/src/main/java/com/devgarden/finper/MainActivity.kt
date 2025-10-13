package com.devgarden.finper

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.devgarden.finper.ui.theme.FinperTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinperTheme {
                val context = LocalContext.current
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    FinanceWelcomeScreen(
                        modifier = Modifier.padding(innerPadding),
                        onStart = {
                            val intent = Intent(context, FormLoginActivity::class.java)
                            context.startActivity(intent)
                        },
                        onAnimateCircle = {
                            val intent = Intent(context, CircleAnimationActivity::class.java)
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FinanceWelcomeScreen(
    modifier: Modifier = Modifier,
    onStart: () -> Unit = {},
    onAnimateCircle: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp

    Surface(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()), // Permite scroll si el contenido no cabe
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = if (isTablet) 48.dp else 24.dp,
                    vertical = 32.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Título principal
                Text(
                    text = "Bienvenido a Finper",
                    style = if (isTablet) MaterialTheme.typography.displaySmall else MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Subtítulo descriptivo
                Text(
                    text = "Tu asistente para controlar tus finanzas.",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                // Botón principal
                Button(
                    onClick = onStart,
                    modifier = Modifier
                        .then(
                            if (isTablet || isLandscape) {
                                Modifier.widthIn(min = 250.dp) // Ancho fijo en tablet o landscape
                            } else {
                                Modifier.fillMaxWidth() // Ancho completo en móvil portrait
                            }
                        )
                        .height(50.dp)
                ) {
                    Text(
                        text = "Comenzar",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón para animar círculo
                Button(
                    onClick = onAnimateCircle,
                    modifier = Modifier
                        .then(
                            if (isTablet || isLandscape) {
                                Modifier.widthIn(min = 250.dp) // Ancho fijo en tablet o landscape
                            } else {
                                Modifier.fillMaxWidth() // Ancho completo en móvil portrait
                            }
                        )
                        .height(50.dp)
                ) {
                    Text(
                        text = "Animar círculo",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

// --- Previews para diferentes configuraciones ---

@Preview(name = "Phone Portrait", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
fun FinanceWelcomeScreenPhonePortraitPreview() {
    FinperTheme {
        FinanceWelcomeScreen()
    }
}

@Preview(name = "Phone Landscape", showBackground = true, widthDp = 780, heightDp = 360)
@Composable
fun FinanceWelcomeScreenPhoneLandscapePreview() {
    FinperTheme {
        FinanceWelcomeScreen()
    }
}

@Preview(name = "Dark Mode", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun FinanceWelcomeScreenDarkPreview() {
    FinperTheme {
        FinanceWelcomeScreen()
    }
}

@Preview(name = "Large Font Scale", showBackground = true, fontScale = 1.5f)
@Composable
fun FinanceWelcomeScreenLargeFontPreview() {
    FinperTheme {
        FinanceWelcomeScreen()
    }
}

@Preview(name = "Tablet Portrait", showBackground = true, widthDp = 800, heightDp = 1280)
@Composable
fun FinanceWelcomeScreenTabletPreview() {
    FinperTheme {
        FinanceWelcomeScreen()
    }
}