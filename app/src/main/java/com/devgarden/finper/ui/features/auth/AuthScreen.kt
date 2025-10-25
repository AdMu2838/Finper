package com.devgarden.finper.ui.features.auth



import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devgarden.finper.R
import com.devgarden.finper.ui.theme.DarkTextColor
import com.devgarden.finper.ui.theme.FinperTheme
import com.devgarden.finper.ui.theme.GrayTextColor
import com.devgarden.finper.ui.theme.LightBackground
import com.devgarden.finper.ui.theme.LightGreenGray
import com.devgarden.finper.ui.theme.PrimaryGreen

@Composable
fun AuthScreen(
    onLoginClicked: () -> Unit,
    onRegisterClicked: () -> Unit,
    onForgotPasswordClicked: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = LightBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Sección del Logo y Título
            Image(
                painter = painterResource(id = R.drawable.ic_logo_finper_v2),
                contentDescription = "Logo de FinPer",
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "FinPer",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = DarkTextColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Administra tus finanzas personales",
                fontSize = 16.sp,
                color = GrayTextColor,
                textAlign = TextAlign.Center
            )

            // Espacio grande antes de los botones
            Spacer(modifier = Modifier.height(64.dp))

            // Botón Ingresar
            Button(
                onClick = onLoginClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text(text = "Ingresar", fontSize = 18.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón Registrarte
            Button(
                onClick = onRegisterClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LightGreenGray)
            ) {
                Text(text = "Registrarte", fontSize = 18.sp, color = DarkTextColor)
            }

            // Botón Olvidaste tu contraseña
            TextButton(onClick = onForgotPasswordClicked, modifier = Modifier.padding(top = 8.dp)) {
                Text(
                    text = "Olvidaste tu contraseña?",
                    color = GrayTextColor,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Preview(showBackground = true, device = "id:pixel_6")
@Composable
fun AuthScreenPreview() {
    FinperTheme {
        AuthScreen(
            onLoginClicked = {},
            onRegisterClicked = {},
            onForgotPasswordClicked = {}
        )
    }
}