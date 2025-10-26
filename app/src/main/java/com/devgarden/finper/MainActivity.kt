package com.devgarden.finper

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import com.devgarden.finper.navigation.AppNavigation
import com.devgarden.finper.ui.theme.FinperTheme
import com.devgarden.finper.ui.viewmodel.UserViewModel


class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Bloquear orientación a portrait en tiempo de ejecución
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        setContent {
            FinperTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val userViewModel: UserViewModel = viewModel()
                    AppNavigation(userViewModel)
                }
            }
        }
    }
}
