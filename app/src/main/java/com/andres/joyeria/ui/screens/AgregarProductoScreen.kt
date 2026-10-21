package com.andres.joyeria.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andres.joyeria.data.api.CrearProductoRequest
import com.andres.joyeria.data.api.RetrofitClient
import com.andres.joyeria.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AgregarProductoScreen(
    token: String,
    onVolver: () -> Unit,
    onProductoCreado: () -> Unit
) {
    var sku by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var material by remember { mutableStateOf("") }
    var peso by remember { mutableStateOf("") }
    var talla by remember { mutableStateOf("") }
    var precioCosto by remember { mutableStateOf("") }
    var precioVenta by remember { mutableStateOf("") }
    var stockActual by remember { mutableStateOf("0") }
    var stockMinimo by remember { mutableStateOf("0") }

    var mensaje by remember { mutableStateOf("") }
    var guardando by remember { mutableStateOf(false) }

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
                        text = "Productos",
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
                text = "CATÁLOGO / ALTA DE PRODUCTO",
                style = MaterialTheme.typography.labelMedium,
                color = JoyeriaDorado,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Nuevo producto",
                style = MaterialTheme.typography.headlineLarge,
                color = JoyeriaOscuro,
                fontWeight = FontWeight.Normal
            )

            Text(
                text = "Completa los datos de la pieza para añadirla a tu catálogo.",
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
                valor = categoria,
                etiqueta = "Categoría (ID) *",
                placeholder = "Seleccionar categoría",
                cambio = { categoria = it },
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
            // SECCIÓN: INVENTARIO
            // ==================================
            Text(
                text = "Inventario",
                style = MaterialTheme.typography.titleLarge,
                color = JoyeriaOscuro
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    CampoFormulario(
                        valor = stockActual,
                        etiqueta = "Stock actual",
                        placeholder = "0",
                        cambio = { stockActual = it },
                        numerico = true
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    CampoFormulario(
                        valor = stockMinimo,
                        etiqueta = "Stock mínimo",
                        placeholder = "0",
                        cambio = { stockMinimo = it },
                        numerico = true
                    )
                }
            }

            Text(
                text = "Registra las existencias y el mínimo de la pieza.",
                style = MaterialTheme.typography.bodySmall,
                color = JoyeriaGris
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = JoyeriaBorde)

            Text(
                text = "Podrás agregar fotografías después de guardar.",
                style = MaterialTheme.typography.bodySmall,
                color = JoyeriaDorado
            )

            if (mensaje.isNotBlank()) {
                Text(
                    text = mensaje,
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
                    if (sku.isBlank() || nombre.isBlank() || categoria.isBlank() || precioCosto.isBlank() || precioVenta.isBlank()) {
                        mensaje = "Por favor completa los campos obligatorios (*)."
                        return@Button
                    }

                    guardando = true
                    mensaje = ""

                    scope.launch {
                        try {
                            RetrofitClient.api.crearProducto(
                                authorization = "Bearer $token",
                                producto = CrearProductoRequest(
                                    sku = sku,
                                    nombre = nombre,
                                    descripcion = descripcion.ifBlank { null },
                                    id_categoria = categoria.toIntOrNull() ?: 1,
                                    material = material.ifBlank { "No especificado" },
                                    peso_gr = peso.toDoubleOrNull(),
                                    talla_medida = talla.ifBlank { null },
                                    precio_costo = precioCosto.toDoubleOrNull() ?: 0.0,
                                    precio_venta = precioVenta.toDoubleOrNull() ?: 0.0,
                                    stock_actual = stockActual.toIntOrNull() ?: 0,
                                    stock_minimo = stockMinimo.toIntOrNull() ?: 0
                                )
                            )
                            onProductoCreado()
                        } catch (e: Exception) {
                            mensaje = "Error al crear: ${e.message}"
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
                    Text("Guardar producto", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
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

@Composable
fun CampoFormulario(
    valor: String,
    etiqueta: String,
    placeholder: String,
    cambio: (String) -> Unit,
    numerico: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodyMedium,
            color = JoyeriaOscuro,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = valor,
            onValueChange = cambio,
            placeholder = { Text(placeholder, color = JoyeriaGris) },
            keyboardOptions = KeyboardOptions(
                keyboardType = if (numerico) KeyboardType.Decimal else KeyboardType.Text
            ),
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
