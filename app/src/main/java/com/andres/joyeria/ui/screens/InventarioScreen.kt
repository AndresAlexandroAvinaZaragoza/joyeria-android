package com.andres.joyeria.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andres.joyeria.data.api.ActualizarInventarioRequest
import com.andres.joyeria.data.api.Inventario
import com.andres.joyeria.data.api.RetrofitClient
import com.andres.joyeria.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun InventarioScreen(
    token: String,
    onVolver: () -> Unit,
    onNavigateTab: (String) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var listaInventario by remember { mutableStateOf<List<Inventario>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    var itemEditando by remember { mutableStateOf<Inventario?>(null) }
    var stockActualInput by remember { mutableStateOf("") }
    var stockMinimoInput by remember { mutableStateOf("") }
    var guardando by remember { mutableStateOf(false) }

    suspend fun cargarInventario() {
        cargando = true
        error = null
        try {
            val respuesta = RetrofitClient.api.obtenerInventario("Bearer $token")
            listaInventario = respuesta.inventarios
        } catch (e: Exception) {
            error = "Error al cargar inventario: ${e.message}"
        } finally {
            cargando = false
        }
    }

    LaunchedEffect(token) {
        cargarInventario()
    }

    fun guardarInventario() {
        val item = itemEditando ?: return
        val nuevoStock = stockActualInput.toIntOrNull()
        val nuevoMinimo = stockMinimoInput.toIntOrNull()

        if (nuevoStock == null || nuevoStock < 0) {
            scope.launch { snackbarHostState.showSnackbar("Ingresa un stock actual válido") }
            return
        }

        scope.launch {
            guardando = true
            try {
                val respuesta = RetrofitClient.api.actualizarInventario(
                    authorization = "Bearer $token",
                    inventarioId = item.id_inventario,
                    request = ActualizarInventarioRequest(
                        stock_actual = nuevoStock,
                        stock_minimo = nuevoMinimo ?: item.stock_minimo
                    )
                )

                if (respuesta.message.isNotBlank()) {
                    itemEditando = null
                    cargarInventario()
                    snackbarHostState.showSnackbar("Inventario actualizado correctamente")
                } else {
                    snackbarHostState.showSnackbar("Error al actualizar inventario")
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
                currentTab = "productos",
                onTabSelected = { tab ->
                    if (tab == "inicio") onVolver()
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
                        text = "Productos",
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
                text = "GESTIÓN DE INVENTARIO",
                style = MaterialTheme.typography.labelMedium,
                color = JoyeriaDorado,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Inventario",
                style = MaterialTheme.typography.headlineLarge,
                color = JoyeriaOscuro,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Control de existencias y stock mínimo.",
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

                listaInventario.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text("No hay registros de inventario.", color = JoyeriaGris)
                    }
                }

                else -> {
                    Text(
                        text = "${listaInventario.size} registro${if (listaInventario.size == 1) "" else "s"} de inventario",
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
                            items = listaInventario,
                            key = { it.id_inventario }
                        ) { item ->
                            InventarioCard(
                                inventario = item,
                                onEditar = {
                                    itemEditando = item
                                    stockActualInput = item.stock_actual.toString()
                                    stockMinimoInput = item.stock_minimo.toString()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    itemEditando?.let { item ->
        AlertDialog(
            onDismissRequest = { if (!guardando) itemEditando = null },
            title = {
                Text(
                    text = "Ajustar inventario",
                    style = MaterialTheme.typography.headlineSmall,
                    color = JoyeriaOscuro
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = item.producto?.nombre ?: "Producto #${item.producto_id}",
                        style = MaterialTheme.typography.titleMedium,
                        color = JoyeriaDorado,
                        fontWeight = FontWeight.Bold
                    )

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Stock actual", style = MaterialTheme.typography.bodyMedium, color = JoyeriaOscuro, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = stockActualInput,
                            onValueChange = { stockActualInput = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                        Text(text = "Stock mínimo", style = MaterialTheme.typography.bodyMedium, color = JoyeriaOscuro, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = stockMinimoInput,
                            onValueChange = { stockMinimoInput = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { guardarInventario() },
                        enabled = !guardando,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = JoyeriaOscuro, contentColor = JoyeriaBlancoCampo),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (guardando) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = JoyeriaBlancoCampo)
                        } else {
                            Text("Guardar cambios", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        }
                    }

                    OutlinedButton(
                        onClick = { itemEditando = null },
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
fun InventarioCard(
    inventario: Inventario,
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
            val producto = inventario.producto
            Text(
                text = "SKU: ${producto?.sku ?: "N/D"} · ${producto?.categoria?.nombre ?: "Sin categoría"}",
                style = MaterialTheme.typography.labelSmall,
                color = JoyeriaDorado,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = producto?.nombre ?: "Producto #${inventario.producto_id}",
                style = MaterialTheme.typography.titleLarge,
                color = JoyeriaOscuro,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = JoyeriaBorde, thickness = 1.dp)
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Stock actual", style = MaterialTheme.typography.bodySmall, color = JoyeriaGris)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${inventario.stock_actual} unidades",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (inventario.stock_actual <= inventario.stock_minimo) MaterialTheme.colorScheme.error else JoyeriaOscuro,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Stock mínimo", style = MaterialTheme.typography.bodySmall, color = JoyeriaGris)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${inventario.stock_minimo} unidades",
                        style = MaterialTheme.typography.bodyMedium,
                        color = JoyeriaGris,
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
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Ajustar inventario", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
