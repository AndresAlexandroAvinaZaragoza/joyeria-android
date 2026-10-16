
package com.andres.joyeria.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.andres.joyeria.data.api.Pedido
import com.andres.joyeria.data.api.RetrofitClient

@Composable
fun DetallePedidoScreen(
    token: String,
    pedidoId: Int,
    onVolver: () -> Unit
) {
    var pedido by remember {
        mutableStateOf<Pedido?>(null)
    }

    var cargando by remember {
        mutableStateOf(true)
    }

    var error by remember {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(token, pedidoId) {
        cargando = true
        error = null

        try {
            val respuesta = RetrofitClient.api.obtenerDetallePedido(
                authorization = "Bearer $token",
                pedidoId = pedidoId
            )
            pedido = respuesta.pedido
        } catch (e: Exception) {
            error = "Error: ${e.message}"
        } finally {
            cargando = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Spacer(Modifier.height(20.dp))

        Text(
            "Detalle del pedido",
            style = MaterialTheme.typography.headlineMedium
        )

        TextButton(onClick = onVolver) {
            Text("← Volver a pedidos")
        }

        if (cargando) {
            CircularProgressIndicator()
        } else if (error != null) {
            Text(
                error!!,
                color = MaterialTheme.colorScheme.error
            )
        } else {
            pedido?.let { p ->

                Spacer(Modifier.height(12.dp))

                Text(
                    "Pedido #${p.numero_pedido}",
                    style = MaterialTheme.typography.titleLarge
                )

                Text("Estado: ${p.estado}")
                Text("Fecha: ${p.created_at ?: "No disponible"}")

                HorizontalDivider(Modifier.padding(vertical = 16.dp))

                Text(
                    "Datos del cliente",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    "Nombre: ${p.usuario?.nombreVisible() ?: "Sin nombre"}"
                )
                Text("Correo: ${p.usuario?.email ?: "Sin correo"}")

                HorizontalDivider(Modifier.padding(vertical = 16.dp))

                Text(
                    "Productos del pedido",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.height(8.dp))

                p.detalles.orEmpty().forEach { detalle ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                detalle.producto?.nombre
                                    ?: "Producto #${detalle.producto_id}"
                            )
                            Text("Cantidad: ${detalle.cantidad}")
                            Text("Precio unitario: $${detalle.precio_unitario}")
                            Text("Importe: $${detalle.importe}")
                        }
                    }
                }

                HorizontalDivider(Modifier.padding(vertical = 16.dp))

                Text(
                    "Resumen del pedido",
                    style = MaterialTheme.typography.titleMedium
                )

                Text("Subtotal: $${p.subtotal}")
                Text("Costo de envío: $${p.costo_envio}")

                Text(
                    "Total: $${p.total} MXN",
                    style = MaterialTheme.typography.titleLarge
                )

                HorizontalDivider(Modifier.padding(vertical = 16.dp))

                Text(
                    "Información del pago",
                    style = MaterialTheme.typography.titleMedium
                )

                Text("Estado: ${p.pago?.estado ?: "pendiente"}")
                Text(
                    "Método: ${p.pago?.metodo_pago ?: "No registrado"}"
                )

                HorizontalDivider(Modifier.padding(vertical = 16.dp))

                Text(
                    "Información del envío",
                    style = MaterialTheme.typography.titleMedium
                )

                Text("Estado: ${p.envio?.estado ?: "pendiente"}")
                Text(
                    "Paquetería: ${p.envio?.paqueteria ?: "No asignada"}"
                )
                Text(
                    "Número de guía: ${p.envio?.numero_guia ?: "No disponible"}"
                )
            }
        }
    }
}
