package com.andres.joyeria.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andres.joyeria.data.api.LoginRequest
import com.andres.joyeria.data.api.RetrofitClient
import com.andres.joyeria.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoginSuccess: (String, String) -> Unit) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JoyeriaFondo)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // ==================================
            // ICONO DIAMANTE EN CÍRCULO DORADO
            // ==================================
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = JoyeriaBlancoCampo,
                border = BorderStroke(1.5.dp, JoyeriaDorado)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Diamond,
                        contentDescription = "Logo",
                        tint = JoyeriaDorado,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==================================
            // TÍTULOS
            // ==================================
            Text(
                text = "Joyería",
                style = MaterialTheme.typography.headlineLarge,
                color = JoyeriaOscuro,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "PANEL DE ADMINISTRACIÓN",
                style = MaterialTheme.typography.labelMedium,
                color = JoyeriaDorado,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Inicia sesión para administrar tu joyería.",
                style = MaterialTheme.typography.bodyMedium,
                color = JoyeriaGris
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ==================================
            // CAMPO CORREO
            // ==================================
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Correo electrónico",
                    style = MaterialTheme.typography.bodyMedium,
                    color = JoyeriaOscuro,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = {
                        Text("Tu correo electrónico", color = JoyeriaGris)
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JoyeriaDorado,
                        unfocusedBorderColor = JoyeriaBorde,
                        focusedContainerColor = JoyeriaBlancoCampo,
                        unfocusedContainerColor = JoyeriaBlancoCampo
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==================================
            // CAMPO CONTRASEÑA
            // ==================================
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Contraseña",
                    style = MaterialTheme.typography.bodyMedium,
                    color = JoyeriaOscuro,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = {
                        Text("••••••••", color = JoyeriaGris)
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JoyeriaDorado,
                        unfocusedBorderColor = JoyeriaBorde,
                        focusedContainerColor = JoyeriaBlancoCampo,
                        unfocusedContainerColor = JoyeriaBlancoCampo
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ==================================
            // BOTÓN INICIAR SESIÓN
            // ==================================
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        mensaje = "Ingresa tu correo y contraseña."
                        return@Button
                    }

                    cargando = true
                    mensaje = ""

                    scope.launch {
                        try {
                            val respuesta = RetrofitClient.api.login(
                                LoginRequest(
                                    email = email,
                                    password = password
                                )
                            )

                            onLoginSuccess(
                                respuesta.token,
                                respuesta.user.name
                            )

                        } catch (e: Exception) {
                            mensaje = "ERROR: ${e.message ?: "Credenciales incorrectas"}"
                            e.printStackTrace()
                        } finally {
                            cargando = false
                        }
                    }
                },
                enabled = !cargando,
                colors = ButtonDefaults.buttonColors(
                    containerColor = JoyeriaOscuro,
                    contentColor = JoyeriaBlancoCampo
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = JoyeriaBlancoCampo
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Iniciar sesión",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Entrar",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (mensaje.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = mensaje,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ==================================
            // FOOTER
            // ==================================
            HorizontalDivider(
                modifier = Modifier.width(40.dp),
                color = JoyeriaDorado,
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Tu colección. Tu espacio de trabajo.",
                style = MaterialTheme.typography.bodySmall,
                color = JoyeriaGris
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
