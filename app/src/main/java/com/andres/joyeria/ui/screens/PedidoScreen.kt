package com.andres.joyeria.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun PedidosScreen(
    token: String,
    onVolver: () -> Unit,
    onVerDetalle: (Pedido) -> Unit,
    onNavigateTab: (String) -> Unit = {}
) {
    var pedidos by remember { mutableStateOf<List<Pedido>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(token) {
        cargando = true
        error = null
        try {
            val respuesta = RetrofitClient.api.obtenerPedidos("Bearer $token")
            pedidos = respuesta.pedidos
        } catch (e: Exception) {
            error = "Error al obtener pedidos: ${e.message}"
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
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

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
                        text = "Inicio",
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

            Spacer(Modifier.height(16.dp))

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
                text = "Pedidos",
                style = MaterialTheme.typography.headlineLarge,
                color = JoyeriaOscuro,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Consulta los pedidos de tus clientes.",
                style = MaterialTheme.typography.bodyMedium,
                color = JoyeriaGris
            )

            Spacer(Modifier.height(16.dp))

            when {
                cargando -> {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = JoyeriaOscuro)
                    }
                }

                error != null -> {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text(text = error!!, color = MaterialTheme.colorScheme.error)
                    }
                }

                pedidos.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text("Aún no hay pedidos registrados.", color = JoyeriaGris)
                    }
                }

                else -> {
                    Text(
                        text = "${pedidos.size} pedido${if (pedidos.size == 1) "" else "s"} registrado${if (pedidos.size == 1) "" else "s"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = JoyeriaGris
                    )

                    Spacer(Modifier.height(12.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(
                            items = pedidos,
                            key = { it.id_pedidos }
                        ) { pedido ->
                            PedidoCard(
                                pedido = pedido,
                                onVerDetalle = { onVerDetalle(pedido) }
                            )
                        }
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = JoyeriaBlancoCampo),
        border = BorderStroke(1.dp, JoyeriaBorde),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Pedido #${pedido.numero_pedido}",
                style = MaterialTheme.typography.labelSmall,
                color = JoyeriaDorado,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = pedido.usuario?.nombreVisible() ?: "Sin nombre",
                style = MaterialTheme.typography.titleLarge,
                color = JoyeriaOscuro,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val piezas = pedido.detalles?.sumOf { it.cantidad } ?: 0
                Text(
                    text = "$piezas pieza${if (piezas == 1) "" else "s"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = JoyeriaGris
                )

                Text(
                    text = "$" + String.format(Locale.forLanguageTag("es-MX"), "%,.2f", pedido.total.toDoubleOrNull() ?: 0.0) + " MXN",
                    style = MaterialTheme.typography.titleMedium,
                    color = JoyeriaOscuro,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = JoyeriaBorde, thickness = 1.dp)
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Pedido: ", style = MaterialTheme.typography.bodySmall, color = JoyeriaGris)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = JoyeriaDorado.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = pedido.estado.replaceFirstChar { it.uppercase() },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = JoyeriaDorado,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pago completado",
                        style = MaterialTheme.typography.bodySmall,
                        color = JoyeriaGris
                    )
                }

                val envioEstado = pedido.envio?.estado ?: "pendiente"
                Text(
                    text = "Envío: ${envioEstado.replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (envioEstado == "enviado" || envioEstado == "entregado") JoyeriaOscuro else JoyeriaGris
                )
            }

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = onVerDetalle,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = JoyeriaOscuro),
                border = BorderStroke(1.dp, JoyeriaBorde)
            ) {
                Text("Ver detalles", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
