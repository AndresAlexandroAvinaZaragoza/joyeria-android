
package com.andres.joyeria.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.andres.joyeria.data.api.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnviosScreen(
    token: String,
    onVolver: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var envios by remember {
        mutableStateOf<List<Envio>>(emptyList())
    }

    var cargando by remember { mutableStateOf(true) }
    var guardando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var envioSeleccionado by remember { mutableStateOf<Envio?>(null) }

    var estado by remember { mutableStateOf("pendiente") }
    var paqueteria by remember { mutableStateOf("") }
    var numeroGuia by remember { mutableStateOf("") }

    var menuExpandido by remember { mutableStateOf(false) }

    val estados = listOf(
        "pendiente" to "Pendiente",
        "preparando" to "Preparando",
        "enviado" to "Enviado",
        "en_transito" to "En tránsito",
        "entregado" to "Entregado",
        "cancelado" to "Cancelado"
    )

    suspend fun cargarEnvios() {
        cargando = true
        error = null

        try {
            val respuesta = RetrofitClient.api.obtenerEnvios(
                "Bearer $token"
            )
            envios = respuesta.envios
        } catch (e: Exception) {
            error = "Error al cargar envíos: ${e.message}"
        } finally {
            cargando = false
        }
    }

    LaunchedEffect(token) {
        cargarEnvios()
    }

    fun abrirEdicion(envio: Envio) {
        envioSeleccionado = envio
        estado = envio.estado
        paqueteria = envio.paqueteria ?: ""
        numeroGuia = envio.numero_guia ?: ""
    }

    fun guardarEnvio() {
        val seleccionado = envioSeleccionado ?: return

        scope.launch {
            guardando = true

            try {
                val respuesta = RetrofitClient.api.actualizarEnvio(
                    authorization = "Bearer $token",
                    envioId = seleccionado.id_envios,
                    request = ActualizarEnvioRequest(
                        estado = estado,
                        paqueteria = paqueteria.ifBlank { null },
                        numero_guia = numeroGuia.ifBlank { null }
                    )
                )

                if (respuesta.isSuccessful) {
                    envioSeleccionado = null
                    cargarEnvios()
                    snackbarHostState.showSnackbar(
                        "Envío actualizado correctamente"
                    )
                } else {
                    snackbarHostState.showSnackbar(
                        "Error al guardar: HTTP ${respuesta.code()}"
                    )
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar(
                    "Error: ${e.message}"
                )
            } finally {
                guardando = false
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            Spacer(Modifier.height(20.dp))

            Text(
                "Gestión de envíos",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(Modifier.height(8.dp))

            Text("Actualiza la paquetería, guía y estado de cada entrega.")

            Spacer(Modifier.height(12.dp))

            TextButton(onClick = onVolver) {
                Text("← Volver")
            }

            Spacer(Modifier.height(12.dp))

            when {
                cargando -> {
                    CircularProgressIndicator()
                }

                error != null -> {
                    Text(
                        error!!,
                        color = MaterialTheme.colorScheme.error
                    )

                    Button(
                        onClick = {
                            scope.launch { cargarEnvios() }
                        }
                    ) {
                        Text("Reintentar")
                    }
                }

                envios.isEmpty() -> {
                    Text("Aún no hay envíos registrados.")
                }

                else -> {
                    Text("${envios.size} envíos registrados")

                    Spacer(Modifier.height(12.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(
                            items = envios,
                            key = { it.id_envios }
                        ) { envio ->

                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        "Pedido #${envio.pedido?.numero_pedido ?: envio.pedido_id}",
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                    Spacer(Modifier.height(8.dp))

                                    Text(
                                        "Cliente: ${
                                            envio.pedido?.usuario?.nombreVisible()
                                                ?: "Sin nombre"
                                        }"
                                    )

                                    Text("Dirección: ${envio.direccion_destino}")

                                    Text(
                                        "Paquetería: ${envio.paqueteria ?: "Sin asignar"}"
                                    )

                                    Text(
                                        "Guía: ${envio.numero_guia ?: "Sin guía"}"
                                    )

                                    Text(
                                        "Estado: ${
                                            estados.firstOrNull {
                                                it.first == envio.estado
                                            }?.second ?: envio.estado
                                        }"
                                    )

                                    Spacer(Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            abrirEdicion(envio)
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Editar envío")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    envioSeleccionado?.let { envio ->

        AlertDialog(
            onDismissRequest = {
                if (!guardando) envioSeleccionado = null
            },
            title = {
                Text("Editar envío #${envio.id_envios}")
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(
                        rememberScrollState()
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Pedido: ${envio.pedido?.numero_pedido ?: envio.pedido_id}"
                    )

                    OutlinedTextField(
                        value = paqueteria,
                        onValueChange = { paqueteria = it },
                        label = { Text("Paquetería") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = numeroGuia,
                        onValueChange = { numeroGuia = it },
                        label = { Text("Número de guía") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    ExposedDropdownMenuBox(
                        expanded = menuExpandido,
                        onExpandedChange = {
                            menuExpandido = !menuExpandido
                        }
                    ) {
                        OutlinedTextField(
                            value = estados.firstOrNull {
                                it.first == estado
                            }?.second ?: estado,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Estado del envío") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = menuExpandido
                                )
                            },
                            modifier = Modifier
                                .menuAnchor(
                                    ExposedDropdownMenuAnchorType.PrimaryNotEditable
                                )
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = menuExpandido,
                            onDismissRequest = {
                                menuExpandido = false
                            }
                        ) {
                            estados.forEach { (valor, etiqueta) ->
                                DropdownMenuItem(
                                    text = { Text(etiqueta) },
                                    onClick = {
                                        estado = valor
                                        menuExpandido = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { guardarEnvio() },
                    enabled = !guardando
                ) {
                    Text(
                        if (guardando) "Guardando..." else "Guardar"
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        envioSeleccionado = null
                    },
                    enabled = !guardando
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
