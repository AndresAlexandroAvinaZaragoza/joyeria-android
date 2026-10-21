package com.andres.joyeria.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andres.joyeria.data.api.Pedido
import com.andres.joyeria.data.api.RetrofitClient
import com.andres.joyeria.ui.theme.*
import java.util.Locale

@Composable
fun DetallePedidoScreen(
    token: String,
    pedidoId: Int,
    onVolver: () -> Unit,
    onNavigateTab: (String) -> Unit = {}
) {
    var pedido by remember { mutableStateOf<Pedido?>(null) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

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

    Scaffold(
        bottomBar = {
            JoyeriaBottomBar(
                currentTab = "pedidos",
                onTabSelected = { tab ->
                    if (tab == "inicio" || tab == "productos") onVolver()
                    else onNavigateTab(tab)
                }
            )
        },
        containerColor = JoyeriaFondo
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(12.dp))

            // ==================================
            // TOP BAR
            // ==================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onVolver,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = JoyeriaOscuro,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Pedidos",
                        style = MaterialTheme.typography.bodyLarge,
                        color = JoyeriaOscuro
                    )
                }

                Icon(
                    imageVector = Icons.Default.Diamond,
                    contentDescription = "Logo",
                    tint = JoyeriaDorado,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            // ==================================
            // HEADER TÍTULO
            // ==================================
            Text(
                text = "GESTIÓN DE VENTAS",
                style = MaterialTheme.typography.labelMedium,
                color = JoyeriaDorado,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Detalle del pedido",
                style = MaterialTheme.typography.headlineLarge,
                color = JoyeriaOscuro,
                fontWeight = FontWeight.Normal
            )

            if (cargando) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = JoyeriaOscuro)
                }
            } else if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
            } else {
                pedido?.let { p ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pedido #${p.numero_pedido}",
                            style = MaterialTheme.typography.titleMedium,
                            color = JoyeriaDorado,
                            fontWeight = FontWeight.Bold
                        )

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = JoyeriaDorado.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = p.estado.replaceFirstChar { it.uppercase() },
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = JoyeriaDorado,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Text(
                        text = p.created_at ?: "14 oct 2026 · 15:03:26 UTC",
                        style = MaterialTheme.typography.bodySmall,
                        color = JoyeriaGris
                    )

                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(color = JoyeriaBorde)

                    // ==================================
                    // DATOS DEL CLIENTE
                    // ==================================
                    Text(
                        text = "Datos del cliente",
                        style = MaterialTheme.typography.titleLarge,
                        color = JoyeriaOscuro
                    )

                    Text(
                        text = p.usuario?.nombreVisible() ?: "Sin nombre",
                        style = MaterialTheme.typography.titleMedium,
                        color = JoyeriaOscuro,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = p.usuario?.email ?: "Sin correo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = JoyeriaGris
                    )

                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(color = JoyeriaBorde)

                    // ==================================
                    // PRODUCTOS DEL PEDIDO
                    // ==================================
                    Text(
                        text = "Productos del pedido",
                        style = MaterialTheme.typography.titleLarge,
                        color = JoyeriaOscuro
                    )

                    p.detalles.orEmpty().forEach { detalle ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = JoyeriaBlancoCampo),
                            border = BorderStroke(1.dp, JoyeriaBorde),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = detalle.producto?.nombre ?: "Producto #${detalle.producto_id}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = JoyeriaOscuro,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Cantidad: ${detalle.cantidad}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = JoyeriaGris
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Precio unitario", style = MaterialTheme.typography.bodySmall, color = JoyeriaGris)
                                    Text(
                                        text = "$" + String.format(Locale.forLanguageTag("es-MX"), "%,.2f", detalle.precio_unitario.toDoubleOrNull() ?: 0.0),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = JoyeriaOscuro
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Importe", style = MaterialTheme.typography.bodySmall, color = JoyeriaGris)
                                    Text(
                                        text = "$" + String.format(Locale.forLanguageTag("es-MX"), "%,.2f", detalle.importe.toDoubleOrNull() ?: 0.0),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = JoyeriaOscuro,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(color = JoyeriaBorde)

                    // ==================================
                    // RESUMEN DEL PEDIDO
                    // ==================================
                    Text(
                        text = "Resumen del pedido",
                        style = MaterialTheme.typography.titleLarge,
                        color = JoyeriaOscuro
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal", style = MaterialTheme.typography.bodyMedium, color = JoyeriaGris)
                        Text("$" + String.format(Locale.forLanguageTag("es-MX"), "%,.2f", p.subtotal.toDoubleOrNull() ?: 0.0), style = MaterialTheme.typography.bodyMedium, color = JoyeriaOscuro)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Costo de envío", style = MaterialTheme.typography.bodyMedium, color = JoyeriaGris)
                        Text("$" + String.format(Locale.forLanguageTag("es-MX"), "%,.2f", p.costo_envio.toDoubleOrNull() ?: 0.0), style = MaterialTheme.typography.bodyMedium, color = JoyeriaOscuro)
                    }

                    HorizontalDivider(color = JoyeriaBorde)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total", style = MaterialTheme.typography.titleMedium, color = JoyeriaOscuro)
                        Text(
                            text = "$" + String.format(Locale.forLanguageTag("es-MX"), "%,.2f", p.total.toDoubleOrNull() ?: 0.0) + " MXN",
                            style = MaterialTheme.typography.titleLarge,
                            color = JoyeriaOscuro,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(color = JoyeriaBorde)

                    // ==================================
                    // INFORMACIÓN DEL PAGO
                    // ==================================
                    Text(
                        text = "Información del pago",
                        style = MaterialTheme.typography.titleLarge,
                        color = JoyeriaOscuro
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Estado", style = MaterialTheme.typography.bodyMedium, color = JoyeriaGris)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = JoyeriaDorado.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = (p.pago?.estado ?: "completado").replaceFirstChar { it.uppercase() },
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = JoyeriaDorado,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Método", style = MaterialTheme.typography.bodyMedium, color = JoyeriaGris)
                        Text(
                            text = p.pago?.metodo_pago ?: "transferencia_ficticia",
                            style = MaterialTheme.typography.bodyMedium,
                            color = JoyeriaOscuro
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(color = JoyeriaBorde)

                    // ==================================
                    // INFORMACIÓN DEL ENVÍO
                    // ==================================
                    Text(
                        text = "Información del envío",
                        style = MaterialTheme.typography.titleLarge,
                        color = JoyeriaOscuro
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Estado", style = MaterialTheme.typography.bodyMedium, color = JoyeriaGris)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = JoyeriaDorado.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = (p.envio?.estado ?: "pendiente").replaceFirstChar { it.uppercase() },
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = JoyeriaDorado,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Paquetería", style = MaterialTheme.typography.bodyMedium, color = JoyeriaGris)
                        Text(
                            text = p.envio?.paqueteria ?: "No asignada",
                            style = MaterialTheme.typography.bodyMedium,
                            color = JoyeriaOscuro
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Número de guía", style = MaterialTheme.typography.bodyMedium, color = JoyeriaGris)
                        Text(
                            text = p.envio?.numero_guia ?: "No disponible",
                            style = MaterialTheme.typography.bodyMedium,
                            color = JoyeriaOscuro
                        )
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}
