package com.devgarden.finper.ui.features.auth.forgot_password

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devgarden.finper.R
import com.devgarden.finper.ui.theme.DarkTextColor
import com.devgarden.finper.ui.theme.FinperTheme
import com.devgarden.finper.ui.theme.GrayTextColor
import com.devgarden.finper.ui.theme.LightGreenGray
import com.devgarden.finper.ui.theme.PrimaryGreen

@Composable
fun ForgotPasswordScreen(
    onNextClick: (String) -> Unit,
    onLoginClick: () -> Unit,
    onGoogleLoginClick: () -> Unit,
    //onFacebookLoginClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryGreen)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Title: "Olvide Mi Contraseña"
            Text(
                text = "Olvide Mi Contraseña",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 100.dp)
                    .fillMaxHeight(0.3f)
            )

            // White form card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp, vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Card Title
                    Text(
                        text = "Reestablecer Mi Contraseña",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkTextColor,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Email Input Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Ingresa Tu Correo") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = Color.LightGray,
                            focusedLabelColor = PrimaryGreen,
                            unfocusedLabelColor = Color.Gray
                        )
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // "Siguiente" Button
                    Button(
                        onClick = { onNextClick(email) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        Text("Siguiente", fontSize = 18.sp, color = Color.White)
                    }

                    // Spacer to push content to the bottom
                    Spacer(modifier = Modifier.weight(1f))

                    // "Ingresa" Button
                    Button(
                        onClick = onLoginClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LightGreenGray)
                    ) {
                        Text("Ingresa", fontSize = 18.sp, color = DarkTextColor)
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    // Social Login Section
                    Text(
                        text = "o ingresa con",
                        color = GrayTextColor,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        /*IconButton(onClick = onFacebookLoginClick) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_facebook_logo),
                                contentDescription = "Facebook Login",
                                modifier = Modifier.size(40.dp)
                            )
                        }*/
                        IconButton(onClick = onGoogleLoginClick) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_google_logo),
                                contentDescription = "Google Login",
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, device = "id:pixel_6")
@Composable
fun ForgotPasswordScreenPreview() {
    FinperTheme {
        ForgotPasswordScreen(
            onNextClick = {},
            onLoginClick = {},
            onGoogleLoginClick = {},
            //onFacebookLoginClick = {}
        )
    }
}