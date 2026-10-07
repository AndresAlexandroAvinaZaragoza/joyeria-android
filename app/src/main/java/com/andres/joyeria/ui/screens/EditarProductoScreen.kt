package com.andres.joyeria.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.andres.joyeria.data.api.Producto
import com.andres.joyeria.data.api.RetrofitClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarProductoScreen(
    token: String,
    producto: Producto,
    onVolver: () -> Unit,
    onProductoActualizado: () -> Unit
) {
    var sku by remember(producto.id_productos) {
        mutableStateOf(producto.sku)
    }

    var nombre by remember(producto.id_productos) {
        mutableStateOf(producto.nombre)
    }

    var descripcion by remember(producto.id_productos) {
        mutableStateOf(producto.descripcion ?: "")
    }

    var material by remember(producto.id_productos) {
        mutableStateOf(producto.material)
    }

    var peso by remember(producto.id_productos) {
        mutableStateOf(producto.peso_gr ?: "")
    }

    var talla by remember(producto.id_productos) {
        mutableStateOf(producto.talla_medida ?: "")
    }

    var precioCosto by remember(producto.id_productos) {
        mutableStateOf(producto.precio_costo)
    }

    var precioVenta by remember(producto.id_productos) {
        mutableStateOf(producto.precio_venta)
    }

    var categoriaId by remember(producto.id_productos) {
        mutableStateOf(producto.id_categoria.toString())
    }

    var activo by remember(producto.id_productos) {
        mutableStateOf(producto.status)
    }

    var guardando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Editar producto",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "ID: ${producto.id_productos}",
            style = MaterialTheme.typography.bodySmall
        )

        OutlinedTextField(
            value = sku,
            onValueChange = { sku = it },
            label = { Text("SKU") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        OutlinedTextField(
            value = material,
            onValueChange = { material = it },
            label = { Text("Material") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = peso,
            onValueChange = { peso = it },
            label = { Text("Peso en gramos") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            )
        )

        OutlinedTextField(
            value = talla,
            onValueChange = { talla = it },
            label = { Text("Talla o medida") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = precioCosto,
            onValueChange = { precioCosto = it },
            label = { Text("Precio de costo") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            )
        )

        OutlinedTextField(
            value = precioVenta,
            onValueChange = { precioVenta = it },
            label = { Text("Precio de venta") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            )
        )

        OutlinedTextField(
            value = categoriaId,
            onValueChange = { categoriaId = it },
            label = { Text("ID de categoría") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Producto activo")

            Switch(
                checked = activo,
                onCheckedChange = { activo = it }
            )
        }

        if (error != null) {
            Text(
                text = error ?: "",
                color = MaterialTheme.colorScheme.error
            )
        }

        Button(
            onClick = {
                val costo = precioCosto.toDoubleOrNull()
                val venta = precioVenta.toDoubleOrNull()
                val pesoNumero = if (peso.isBlank()) {
                    null
                } else {
                    peso.toDoubleOrNull()
                }
                val categoria = categoriaId.toIntOrNull()

                when {
                    sku.isBlank() || nombre.isBlank() ->
                        error = "El SKU y el nombre son obligatorios."

                    costo == null || costo < 0 ->
                        error = "Ingresa un precio de costo válido."

                    venta == null || venta < 0 ->
                        error = "Ingresa un precio de venta válido."

                    peso.isNotBlank() &&
                            (pesoNumero == null || pesoNumero < 0) ->
                        error = "Ingresa un peso válido."

                    categoria == null || categoria <= 0 ->
                        error = "Ingresa una categoría válida."

                    else -> {
                        error = null

                        val datos = ActualizarProductoRequest(
                            sku = sku.trim(),
                            nombre = nombre.trim(),
                            descripcion = descripcion.ifBlank { null },
                            material = material.trim(),
                            peso_gr = pesoNumero,
                            talla_medida = talla.ifBlank { null },
                            precio_costo = costo,
                            precio_venta = venta,
                            status = activo,
                            id_categoria = categoria
                        )

                        scope.launch {
                            guardando = true

                            try {
                                val respuesta =
                                    RetrofitClient.api.actualizarProducto(
                                        authorization = "Bearer $token",
                                        productoId = producto.id_productos,
                                        datos = datos
                                    )

                                if (respuesta.isSuccessful) {
                                    onProductoActualizado()
                                } else {
                                    val detalle = respuesta.errorBody()
                                        ?.string()
                                        ?.take(500)

                                    error = "Error ${respuesta.code()}: " +
                                            (detalle ?: "No se pudo actualizar.")
                                }
                            } catch (e: Exception) {
                                error = e.message
                                    ?: "Error de conexión con Laravel."
                            } finally {
                                guardando = false
                            }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !guardando
        ) {
            if (guardando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Guardar cambios")
            }
        }

        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth(),
            enabled = !guardando
        ) {
            Text("Cancelar")
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}