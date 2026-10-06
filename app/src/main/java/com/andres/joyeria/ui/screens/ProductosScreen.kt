package com.andres.joyeria.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.andres.joyeria.data.api.Producto
import com.andres.joyeria.data.api.RetrofitClient

@Composable
fun ProductosScreen(
    token: String,
    onVolver: () -> Unit,
    onAgregarProducto: () -> Unit,
    onAgregarFoto: (Producto) -> Unit
) {
    var productos by remember {
        mutableStateOf<List<Producto>>(emptyList())
    }

    var cargando by remember {
        mutableStateOf(true)
    }

    var error by remember {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(Unit) {
        try {
            val respuesta = RetrofitClient.api.obtenerProductos(
                "Bearer $token"
            )

            productos = respuesta.productos

        } catch (e: Exception) {
            error = e.message
        } finally {
            cargando = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Spacer(modifier = Modifier.height(30.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Productos",
                style = MaterialTheme.typography.headlineMedium
            )

            TextButton(
                onClick = onVolver
            ) {
                Text("Volver")
            }

            Button(
                onClick = onAgregarProducto,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("+ Agregar producto")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            cargando -> {
                CircularProgressIndicator()
            }

            error != null -> {
                Text("Error: $error")
            }

            productos.isEmpty() -> {
                Text("No hay productos registrados.")
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = productos,
                        key = { it.id_productos }
                    ) { producto ->

                        ProductoCard(
                            producto = producto,
                            onAgregarFoto = onAgregarFoto
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductoCard(
    producto: Producto,
    onAgregarFoto: (Producto) -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = producto.nombre,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text("SKU: ${producto.sku}")
            Text("Material: ${producto.material}")
            Text(
                "Categoría: ${producto.categoria?.nombre ?: "Sin categoría"}"
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$${producto.precio_venta}",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = producto.inventario?.let {
                    "Stock: ${it.stock_actual}"
                } ?: "Sin inventario"
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    onAgregarFoto(producto)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("📷 Agregar fotografía")
            }
        }
    }
}