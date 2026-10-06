package com.andres.joyeria.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.andres.joyeria.data.api.LoginRequest
import com.andres.joyeria.data.api.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun LoginScreen( onLoginSuccess: (String, String) -> Unit) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Joyería",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Panel de administración",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = {
                Text("Correo electrónico")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = {
                Text("Contraseña")
            },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

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
                        mensaje = "ERROR: ${e.javaClass.simpleName}: ${e.message}"
                        e.printStackTrace()
                    } finally {

                        cargando = false
                    }
                }
            },
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth()
        ) {

            if (cargando) {

                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )

            } else {

                Text("Iniciar sesión")
            }
        }

        if (mensaje.isNotEmpty()) {

            Spacer(modifier = Modifier.height(16.dp))

            Text(mensaje)
        }
    }
}