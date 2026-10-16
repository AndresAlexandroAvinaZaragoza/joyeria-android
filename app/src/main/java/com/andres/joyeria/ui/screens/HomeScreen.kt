
package com.andres.joyeria.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    nombre: String,
    onProductosClick: () -> Unit,
    onFotografiasClick: () -> Unit,
    onPedidosClick: () -> Unit,
    onCerrarSesion: () -> Unit,
    onEnviosClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "Joyería",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Hola, $nombre",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        // =====================================
        // PRODUCTOS
        // =====================================

        Button(
            onClick = onProductosClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📦 Productos")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // =====================================
        // FOTOGRAFÍAS
        // =====================================

        Button(
            onClick = onFotografiasClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📷 Fotografías")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // =====================================
        // PEDIDOS
        // =====================================

        Button(
            onClick = onPedidosClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🧾 Pedidos")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onEnviosClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🚚 Envíos")
        }

        Spacer(modifier = Modifier.weight(1f))

        // =====================================
        // CERRAR SESIÓN
        // =====================================

        OutlinedButton(
            onClick = onCerrarSesion,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión")
        }
    }
}
