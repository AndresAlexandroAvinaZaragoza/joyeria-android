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
import com.andres.joyeria.data.api.CrearProductoRequest
import com.andres.joyeria.data.api.RetrofitClient
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
    var stockActual by remember { mutableStateOf("") }
    var stockMinimo by remember { mutableStateOf("") }

    var mensaje by remember { mutableStateOf("") }
    var guardando by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        Spacer(modifier = Modifier.height(30.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = "Nuevo producto",
                style = MaterialTheme.typography.headlineMedium
            )

            TextButton(
                onClick = onVolver
            ) {
                Text("Cancelar")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        CampoTexto(
            valor = sku,
            etiqueta = "SKU",
            cambio = { sku = it }
        )

        CampoTexto(
            valor = nombre,
            etiqueta = "Nombre",
            cambio = { nombre = it }
        )

        CampoTexto(
            valor = descripcion,
            etiqueta = "Descripción",
            cambio = { descripcion = it }
        )

        CampoTexto(
            valor = categoria,
            etiqueta = "ID categoría",
            cambio = { categoria = it },
            numerico = true
        )

        CampoTexto(
            valor = material,
            etiqueta = "Material",
            cambio = { material = it }
        )

        CampoTexto(
            valor = peso,
            etiqueta = "Peso (gr)",
            cambio = { peso = it },
            numerico = true
        )

        CampoTexto(
            valor = talla,
            etiqueta = "Talla / medida",
            cambio = { talla = it }
        )

        CampoTexto(
            valor = precioCosto,
            etiqueta = "Precio costo",
            cambio = { precioCosto = it },
            numerico = true
        )

        CampoTexto(
            valor = precioVenta,
            etiqueta = "Precio venta",
            cambio = { precioVenta = it },
            numerico = true
        )

        CampoTexto(
            valor = stockActual,
            etiqueta = "Stock actual",
            cambio = { stockActual = it },
            numerico = true
        )

        CampoTexto(
            valor = stockMinimo,
            etiqueta = "Stock mínimo",
            cambio = { stockMinimo = it },
            numerico = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {

                if (
                    sku.isBlank() ||
                    nombre.isBlank() ||
                    categoria.isBlank() ||
                    material.isBlank() ||
                    precioCosto.isBlank() ||
                    precioVenta.isBlank() ||
                    stockActual.isBlank() ||
                    stockMinimo.isBlank()
                ) {
                    mensaje = "Completa los campos obligatorios."
                    return@Button
                }

                val categoriaNumero = categoria.toIntOrNull()
                val costoNumero = precioCosto.toDoubleOrNull()
                val ventaNumero = precioVenta.toDoubleOrNull()
                val stockNumero = stockActual.toIntOrNull()
                val minimoNumero = stockMinimo.toIntOrNull()

                if (
                    categoriaNumero == null ||
                    costoNumero == null ||
                    ventaNumero == null ||
                    stockNumero == null ||
                    minimoNumero == null
                ) {
                    mensaje = "Revisa los valores numéricos."
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
                                id_categoria = categoriaNumero,
                                material = material,
                                peso_gr = peso.toDoubleOrNull(),
                                talla_medida = talla.ifBlank { null },
                                precio_costo = costoNumero,
                                precio_venta = ventaNumero,
                                stock_actual = stockNumero,
                                stock_minimo = minimoNumero
                            )
                        )

                        onProductoCreado()

                    } catch (e: Exception) {

                        mensaje =
                            "No se pudo guardar el producto: ${e.message}"

                    } finally {

                        guardando = false
                    }
                }
            },

            enabled = !guardando,

            modifier = Modifier.fillMaxWidth()
        ) {

            if (guardando) {

                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )

            } else {

                Text("Guardar producto")
            }
        }

        if (mensaje.isNotEmpty()) {

            Spacer(modifier = Modifier.height(12.dp))

            Text(mensaje)
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun CampoTexto(
    valor: String,
    etiqueta: String,
    cambio: (String) -> Unit,
    numerico: Boolean = false
) {

    OutlinedTextField(
        value = valor,
        onValueChange = cambio,
        label = {
            Text(etiqueta)
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = if (numerico) {
                KeyboardType.Decimal
            } else {
                KeyboardType.Text
            }
        ),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(12.dp))
}