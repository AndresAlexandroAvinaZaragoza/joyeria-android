package com.andres.joyeria.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andres.joyeria.data.api.*
import com.andres.joyeria.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnviosScreen(
    token: String,
    onVolver: () -> Unit,
    onNavigateTab: (String) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var envios by remember { mutableStateOf<List<Envio>>(emptyList()) }
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
            val respuesta = RetrofitClient.api.obtenerEnvios("Bearer $token")
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
                    snackbarHostState.showSnackbar("Envío actualizado correctamente")
                } else {
                    snackbarHostState.showSnackbar("Error al guardar: HTTP ${respuesta.code()}")
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Error: ${e.message}")
            } finally {
                guardando = false
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            JoyeriaBottomBar(
                currentTab = "envios",
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
                text = "GESTIÓN DE ENTREGAS",
                style = MaterialTheme.typography.labelMedium,
                color = JoyeriaDorado,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Envíos",
                style = MaterialTheme.typography.headlineLarge,
                color = JoyeriaOscuro,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Actualiza la paquetería, guía y estado de cada entrega.",
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
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = error!!, color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { scope.launch { cargarEnvios() } }) {
                                Text("Reintentar")
                            }
                        }
                    }
                }

                envios.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text("Aún no hay envíos registrados.", color = JoyeriaGris)
                    }
                }

                else -> {
                    Text(
                        text = "${envios.size} envío${if (envios.size == 1) "" else "s"} registrado${if (envios.size == 1) "" else "s"}",
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
                            items = envios,
                            key = { it.id_envios }
                        ) { envio ->
                            EnvioCard(
                                envio = envio,
                                estados = estados,
                                onEditar = { abrirEdicion(envio) }
                            )
                        }
                    }
                }
            }
        }
    }

    envioSeleccionado?.let { envio ->
        AlertDialog(
            onDismissRequest = { if (!guardando) envioSeleccionado = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Editar envío #${envio.id_envios}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = JoyeriaOscuro
                    )
                    IconButton(onClick = { if (!guardando) envioSeleccionado = null }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar", tint = JoyeriaGris)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column {
                        Text(text = "Pedido", style = MaterialTheme.typography.bodySmall, color = JoyeriaGris)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = envio.pedido?.numero_pedido ?: envio.pedido_id.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = JoyeriaDorado,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Paquetería", style = MaterialTheme.typography.bodyMedium, color = JoyeriaOscuro, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = paqueteria,
                            onValueChange = { paqueteria = it },
                            placeholder = { Text("Ej. FEDEX, DHL...", color = JoyeriaGris) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = JoyeriaDorado,
                                unfocusedBorderColor = JoyeriaBorde,
                                focusedContainerColor = JoyeriaBlancoCampo,
                                unfocusedContainerColor = JoyeriaBlancoCampo
                            )
                        )
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Número de guía", style = MaterialTheme.typography.bodyMedium, color = JoyeriaOscuro, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = numeroGuia,
                            onValueChange = { numeroGuia = it },
                            placeholder = { Text("Número de guía", color = JoyeriaGris) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = JoyeriaDorado,
                                unfocusedBorderColor = JoyeriaBorde,
                                focusedContainerColor = JoyeriaBlancoCampo,
                                unfocusedContainerColor = JoyeriaBlancoCampo
                            )
                        )
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Estado del envío", style = MaterialTheme.typography.bodyMedium, color = JoyeriaOscuro, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(6.dp))
                        ExposedDropdownMenuBox(
                            expanded = menuExpandido,
                            onExpandedChange = { menuExpandido = !menuExpandido }
                        ) {
                            OutlinedTextField(
                                value = estados.firstOrNull { it.first == estado }?.second ?: estado,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuExpandido) },
                                modifier = Modifier
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = JoyeriaDorado,
                                    unfocusedBorderColor = JoyeriaBorde,
                                    focusedContainerColor = JoyeriaBlancoCampo,
                                    unfocusedContainerColor = JoyeriaBlancoCampo
                                )
                            )

                            ExposedDropdownMenu(
                                expanded = menuExpandido,
                                onDismissRequest = { menuExpandido = false }
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
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { guardarEnvio() },
                        enabled = !guardando,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = JoyeriaOscuro, contentColor = JoyeriaBlancoCampo),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (guardando) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = JoyeriaBlancoCampo)
                        } else {
                            Text("Guardar", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        }
                    }

                    OutlinedButton(
                        onClick = { envioSeleccionado = null },
                        enabled = !guardando,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = JoyeriaOscuro),
                        border = BorderStroke(1.dp, JoyeriaBorde)
                    ) {
                        Text("Cancelar", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        )
    }
}

@Composable
fun EnvioCard(
    envio: Envio,
    estados: List<Pair<String, String>>,
    onEditar: () -> Unit
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
                text = "Pedido #${envio.pedido?.numero_pedido ?: envio.pedido_id}",
                style = MaterialTheme.typography.labelSmall,
                color = JoyeriaDorado,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = envio.pedido?.usuario?.nombreVisible() ?: "Sin nombre",
                style = MaterialTheme.typography.titleLarge,
                color = JoyeriaOscuro,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Dirección de entrega",
                style = MaterialTheme.typography.bodySmall,
                color = JoyeriaGris
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = envio.direccion_destino ?: "Sin dirección registrada",
                style = MaterialTheme.typography.bodyMedium,
                color = JoyeriaOscuro
            )

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = JoyeriaBorde, thickness = 1.dp)
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Paquetería", style = MaterialTheme.typography.bodySmall, color = JoyeriaGris)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = envio.paqueteria ?: "Sin asignar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = JoyeriaOscuro,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Número de guía", style = MaterialTheme.typography.bodySmall, color = JoyeriaGris)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = envio.numero_guia ?: "Sin guía",
                        style = MaterialTheme.typography.bodyMedium,
                        color = JoyeriaOscuro,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Estado", style = MaterialTheme.typography.bodySmall, color = JoyeriaGris)

                val estadoStr = estados.firstOrNull { it.first == envio.estado }?.second ?: envio.estado
                val esEnviado = envio.estado == "enviado" || envio.estado == "entregado"

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (esEnviado) JoyeriaOscuro.copy(alpha = 0.1f) else JoyeriaDorado.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = estadoStr,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (esEnviado) JoyeriaOscuro else JoyeriaDorado,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = onEditar,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = JoyeriaOscuro),
                border = BorderStroke(1.dp, JoyeriaBorde)
            ) {
                Text("Editar envío", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
