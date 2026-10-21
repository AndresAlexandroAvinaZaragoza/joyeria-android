package com.andres.joyeria.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andres.joyeria.data.api.Producto
import com.andres.joyeria.data.api.RetrofitClient
import com.andres.joyeria.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun EditarProductoScreen(
    token: String,
    producto: Producto,
    onVolver: () -> Unit,
    onProductoActualizado: () -> Unit
) {
    var sku by remember(producto.id_productos) { mutableStateOf(producto.sku) }
    var nombre by remember(producto.id_productos) { mutableStateOf(producto.nombre) }
    var descripcion by remember(producto.id_productos) { mutableStateOf(producto.descripcion ?: "") }
    var material by remember(producto.id_productos) { mutableStateOf(producto.material) }
    var peso by remember(producto.id_productos) { mutableStateOf(producto.peso_gr ?: "") }
    var talla by remember(producto.id_productos) { mutableStateOf(producto.talla_medida ?: "") }
    var precioCosto by remember(producto.id_productos) { mutableStateOf(producto.precio_costo) }
    var precioVenta by remember(producto.id_productos) { mutableStateOf(producto.precio_venta) }
    var categoriaId by remember(producto.id_productos) { mutableStateOf(producto.id_categoria.toString()) }
    var activo by remember(producto.id_productos) { mutableStateOf(producto.status) }

    var guardando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    Scaffold(
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
            Spacer(modifier = Modifier.height(12.dp))

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
                        text = "Detalle del producto",
                        style = MaterialTheme.typography.bodyLarge,
                        color = JoyeriaOscuro
                    )
                }

                TextButton(
                    onClick = onVolver
                ) {
                    Text(
                        text = "Cancelar",
                        style = MaterialTheme.typography.bodyLarge,
                        color = JoyeriaDorado,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ==================================
            // HEADER TÍTULO
            // ==================================
            Text(
                text = "PRODUCTO #${producto.id_productos} · SKU ${producto.sku}",
                style = MaterialTheme.typography.labelMedium,
                color = JoyeriaDorado,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Editar producto",
                style = MaterialTheme.typography.headlineLarge,
                color = JoyeriaOscuro,
                fontWeight = FontWeight.Normal
            )

            Text(
                text = "Actualiza la ficha y guarda los cambios.",
                style = MaterialTheme.typography.bodyMedium,
                color = JoyeriaGris
            )

            Text(
                text = "Los campos con * son obligatorios.",
                style = MaterialTheme.typography.bodySmall,
                color = JoyeriaGris
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = JoyeriaBorde)

            // ==================================
            // SECCIÓN: INFORMACIÓN DE LA PIEZA
            // ==================================
            Text(
                text = "Información de la pieza",
                style = MaterialTheme.typography.titleLarge,
                color = JoyeriaOscuro
            )

            CampoFormulario(
                valor = sku,
                etiqueta = "SKU *",
                placeholder = "Código de producto",
                cambio = { sku = it }
            )

            CampoFormulario(
                valor = nombre,
                etiqueta = "Nombre *",
                placeholder = "Nombre de la pieza",
                cambio = { nombre = it }
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Descripción", style = MaterialTheme.typography.bodyMedium, color = JoyeriaOscuro, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    placeholder = { Text("Describe la pieza", color = JoyeriaGris) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JoyeriaDorado,
                        unfocusedBorderColor = JoyeriaBorde,
                        focusedContainerColor = JoyeriaBlancoCampo,
                        unfocusedContainerColor = JoyeriaBlancoCampo
                    )
                )
            }

            CampoFormulario(
                valor = categoriaId,
                etiqueta = "Categoría (ID) *",
                placeholder = "Seleccionar categoría",
                cambio = { categoriaId = it },
                numerico = true
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = JoyeriaBorde)

            // ==================================
            // SECCIÓN: MATERIAL Y MEDIDAS
            // ==================================
            Text(
                text = "Material y medidas",
                style = MaterialTheme.typography.titleLarge,
                color = JoyeriaOscuro
            )

            CampoFormulario(
                valor = material,
                etiqueta = "Material",
                placeholder = "Ej. Oro 18K",
                cambio = { material = it }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    CampoFormulario(
                        valor = peso,
                        etiqueta = "Peso (g)",
                        placeholder = "0.00",
                        cambio = { peso = it },
                        numerico = true
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    CampoFormulario(
                        valor = talla,
                        etiqueta = "Talla / medida",
                        placeholder = "Talla o medida",
                        cambio = { talla = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = JoyeriaBorde)

            // ==================================
            // SECCIÓN: PRECIOS
            // ==================================
            Text(
                text = "Precios",
                style = MaterialTheme.typography.titleLarge,
                color = JoyeriaOscuro
            )

            Text(
                text = "Importes en pesos mexicanos (MXN).",
                style = MaterialTheme.typography.bodySmall,
                color = JoyeriaGris
            )

            CampoFormulario(
                valor = precioCosto,
                etiqueta = "Precio de costo *",
                placeholder = "$ 0.00",
                cambio = { precioCosto = it },
                numerico = true
            )

            CampoFormulario(
                valor = precioVenta,
                etiqueta = "Precio de venta *",
                placeholder = "$ 0.00",
                cambio = { precioVenta = it },
                numerico = true
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = JoyeriaBorde)

            // ==================================
            // SECCIÓN: INVENTARIO Y ESTADO
            // ==================================
            Text(
                text = "Inventario",
                style = MaterialTheme.typography.titleLarge,
                color = JoyeriaOscuro
            )

            val stockActualStr = producto.inventario?.stock_actual?.toString() ?: "Sin registrar"
            val stockMinimoStr = producto.inventario?.stock_minimo?.toString() ?: "Sin registrar"

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Stock actual", style = MaterialTheme.typography.bodyMedium, color = JoyeriaOscuro, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = stockActualStr,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = JoyeriaBorde,
                                unfocusedBorderColor = JoyeriaBorde,
                                focusedContainerColor = JoyeriaBlancoCampo,
                                unfocusedContainerColor = JoyeriaBlancoCampo
                            )
                        )
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Stock mínimo", style = MaterialTheme.typography.bodyMedium, color = JoyeriaOscuro, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = stockMinimoStr,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = JoyeriaBorde,
                                unfocusedBorderColor = JoyeriaBorde,
                                focusedContainerColor = JoyeriaBlancoCampo,
                                unfocusedContainerColor = JoyeriaBlancoCampo
                            )
                        )
                    }
                }
            }

            Text(
                text = "Este producto no tiene inventario registrado.",
                style = MaterialTheme.typography.bodySmall,
                color = JoyeriaGris
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Switch Producto activo
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Producto activo", style = MaterialTheme.typography.bodyLarge, color = JoyeriaOscuro, fontWeight = FontWeight.Medium)
                    Text(text = "Disponible en tu catálogo de productos.", style = MaterialTheme.typography.bodySmall, color = JoyeriaGris)
                }
                Switch(
                    checked = activo,
                    onCheckedChange = { activo = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = JoyeriaDorado)
                )
            }

            if (error != null) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==================================
            // BOTONES DE ACCIÓN
            // ==================================
            Button(
                onClick = {
                    if (sku.isBlank() || nombre.isBlank() || categoriaId.isBlank() || precioCosto.isBlank() || precioVenta.isBlank()) {
                        error = "Completa los campos obligatorios."
                        return@Button
                    }

                    guardando = true
                    error = null

                    scope.launch {
                        try {
                            val respuesta = RetrofitClient.api.actualizarProducto(
                                authorization = "Bearer $token",
                                productoId = producto.id_productos,
                                datos = ActualizarProductoRequest(
                                    sku = sku,
                                    nombre = nombre,
                                    descripcion = descripcion.ifBlank { null },
                                    material = material,
                                    peso_gr = peso.toDoubleOrNull(),
                                    talla_medida = talla.ifBlank { null },
                                    precio_costo = precioCosto.toDoubleOrNull() ?: 0.0,
                                    precio_venta = precioVenta.toDoubleOrNull() ?: 0.0,
                                    status = activo,
                                    id_categoria = categoriaId.toIntOrNull() ?: producto.id_categoria
                                )
                            )

                            if (respuesta.isSuccessful) {
                                onProductoActualizado()
                            } else {
                                error = "Error al actualizar. Código: ${respuesta.code()}"
                            }
                        } catch (e: Exception) {
                            error = "Error: ${e.message}"
                        } finally {
                            guardando = false
                        }
                    }
                },
                enabled = !guardando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = JoyeriaOscuro,
                    contentColor = JoyeriaBlancoCampo
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (guardando) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = JoyeriaBlancoCampo)
                } else {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "Guardar", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar cambios", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                }
            }

            OutlinedButton(
                onClick = onVolver,
                enabled = !guardando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = JoyeriaOscuro),
                border = BorderStroke(1.dp, JoyeriaBorde)
            ) {
                Text("Cancelar", style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
