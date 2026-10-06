package com.andres.joyeria.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    nombre: String,
    onProductosClick: () -> Unit,
    onFotografiasClick: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Text("Joyería")
        Text("Hola, $nombre")

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onProductosClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📦 Productos")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onFotografiasClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📷 Fotografías")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🧾 Pedidos - Próximamente")
        }

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(
            onClick = onCerrarSesion,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión")
        }

    }
}