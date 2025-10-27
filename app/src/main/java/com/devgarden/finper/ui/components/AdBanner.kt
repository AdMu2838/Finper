package com.devgarden.finper.ui.components

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds

/**
 * Banner adaptable que se ajusta al tamaño de la pantalla
 * Componente de anuncio no invasivo para la app
 */
@Composable
fun AdaptiveAdBanner(
    modifier: Modifier = Modifier,
    testAdUnitId: String = "ca-app-pub-3940256099942544/6300978111" // ID de prueba de Google
) {
    val context = LocalContext.current
    var adLoaded by remember { mutableStateOf(false) }
    var adError by remember { mutableStateOf<String?>(null) }

    // Inicializar AdMob
    DisposableEffect(Unit) {
        MobileAds.initialize(context) { initializationStatus ->
            Log.d("AdMob", "Initialized: ${initializationStatus.adapterStatusMap}")
        }
        onDispose { }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8F9FA)
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            if (adError != null) {
                // Mostrar un placeholder cuando hay error (solo en desarrollo)
                Text(
                    text = "Espacio publicitario",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 20.dp)
                )
            } else {
                AndroidView(
                    modifier = Modifier.fillMaxWidth(),
                    factory = { ctx ->
                        AdView(ctx).apply {
                            setAdSize(AdSize.BANNER)
                            adUnitId = testAdUnitId

                            adListener = object : AdListener() {
                                override fun onAdLoaded() {
                                    super.onAdLoaded()
                                    adLoaded = true
                                    Log.d("AdMob", "Ad loaded successfully")
                                }

                                override fun onAdFailedToLoad(error: LoadAdError) {
                                    super.onAdFailedToLoad(error)
                                    adError = error.message
                                    Log.e("AdMob", "Ad failed to load: ${error.message} (Code: ${error.code})")
                                }
                            }

                            loadAd(AdRequest.Builder().build())
                        }
                    }
                )
            }
        }
    }
}
