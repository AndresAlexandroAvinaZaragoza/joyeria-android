
package com.andres.joyeria.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.andres.joyeria.data.api.Pedido
import com.andres.joyeria.data.api.RetrofitClient
import java.util.Locale

@Composable
fun PedidosScreen(
    token: String,
    onVolver: () -> Unit,
    onVerDetalle: (Pedido) -> Unit
) {
    var pedidos by remember {
        mutableStateOf<List<Pedido>>(emptyList())
    }

    var cargando by remember {
        mutableStateOf(true)
    }

    var error by remember {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(token) {
        cargando = true
        error = null

        try {
            val respuesta = RetrofitClient.api.obtenerPedidos(
                "Bearer $token"
            )

            pedidos = respuesta.pedidos

        } catch (e: Exception) {
            error = "Error al obtener pedidos: ${e.message}"
        } finally {
            cargando = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Spacer(Modifier.height(20.dp))

        Text(
            text = "Gestión de pedidos",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Consulta los pedidos de tus clientes",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(12.dp))

        TextButton(onClick = onVolver) {
            Text("← Volver")
        }

        Spacer(Modifier.height(16.dp))

        when {
            cargando -> {
                CircularProgressIndicator()
            }

            error != null -> {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error
                )
            }

            pedidos.isEmpty() -> {
                Text("Aún no hay pedidos registrados.")
            }

            else -> {
                Text(
                    text = "${pedidos.size} pedidos registrados",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.height(12.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(
                        items = pedidos,
                        key = { it.id_pedidos }
                    ) { pedido ->
                        PedidoCard(
                            pedido = pedido,
                            onVerDetalle = {
                                onVerDetalle(pedido)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PedidoCard(
    pedido: Pedido,
    onVerDetalle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Pedido #${pedido.numero_pedido}",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(8.dp))

            Text("Cliente: ${pedido.usuario?.nombreVisible() ?: "Sin nombre"}")
            Text("Estado: ${pedido.estado}")

            Text(
                "Productos: ${
                    pedido.detalles?.sumOf { it.cantidad } ?: 0
                } pieza(s)"
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Total: $${pedido.total} MXN",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Pago: ${pedido.pago?.estado ?: "pendiente"}"
            )

            Text(
                text = "Envío: ${pedido.envio?.estado ?: "pendiente"}"
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onVerDetalle,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver detalles")
            }
        }
    }
}
