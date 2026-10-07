
package com.andres.joyeria.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.andres.joyeria.data.api.Producto
import com.andres.joyeria.data.api.ProductoImagen
import com.andres.joyeria.data.api.RetrofitClient
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun ProductosScreen(
    token: String,
    onVolver: () -> Unit,
    onAgregarProducto: () -> Unit,
    onAgregarFoto: (Producto) -> Unit,
    onEditarProducto: (Producto) -> Unit,
    onAdministrarImagenes: (Producto) -> Unit
) {

    // ==========================================
    // ESTADOS
    // ==========================================

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    val scope = rememberCoroutineScope()

    var productos by remember {
        mutableStateOf<List<Producto>>(emptyList())
    }

    var cargando by remember {
        mutableStateOf(true)
    }

    var error by remember {
        mutableStateOf<String?>(null)
    }

    var productoAEliminar by remember {
        mutableStateOf<Producto?>(null)
    }

    var eliminando by remember {
        mutableStateOf(false)
    }

    // ==========================================
    // OBTENER PRODUCTOS DE LARAVEL
    // ==========================================

    LaunchedEffect(token) {

        cargando = true
        error = null

        try {

            val respuesta = RetrofitClient.api.obtenerProductos(
                "Bearer $token"
            )

            productos = respuesta.productos

        } catch (e: Exception) {

            error = e.message ?: "Error al obtener productos"

        } finally {

            cargando = false
        }
    }

    // ==========================================
    // ELIMINAR PRODUCTO
    // ==========================================

    fun eliminarProducto(producto: Producto) {

        scope.launch {

            eliminando = true
            error = null

            try {

                val respuesta = RetrofitClient.api.eliminarProducto(
                    authorization = "Bearer $token",
                    productoId = producto.id_productos
                )

                if (respuesta.isSuccessful) {

                    // Quitar producto de la lista
                    productos = productos.filter {
                        it.id_productos != producto.id_productos
                    }

                    // Cerrar ventana de confirmación
                    productoAEliminar = null

                    // Notificación de éxito
                    snackbarHostState.showSnackbar(
                        message = "Producto '${producto.nombre}' eliminado correctamente",
                        duration = SnackbarDuration.Short
                    )

                } else {

                    error = "No se pudo eliminar el producto. Código: ${respuesta.code()}"

                    snackbarHostState.showSnackbar(
                        message = error ?: "Error al eliminar producto",
                        duration = SnackbarDuration.Short
                    )
                }

            } catch (e: Exception) {

                error = e.message ?: "Error al conectar con Laravel"

                snackbarHostState.showSnackbar(
                    message = "Error: $error",
                    duration = SnackbarDuration.Short
                )

            } finally {

                eliminando = false
            }
        }
    }

    // ==========================================
    // INTERFAZ PRINCIPAL
    // ==========================================

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
        ) {

            Spacer(modifier = Modifier.height(24.dp))

            // ==================================
            // ENCABEZADO
            // ==================================

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "Productos",
                    style = MaterialTheme.typography.headlineMedium
                )

                TextButton(
                    onClick = onVolver
                ) {
                    Text("Volver")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==================================
            // AGREGAR PRODUCTO
            // ==================================

            Button(
                onClick = onAgregarProducto,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("+ Agregar producto")
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ==================================
            // MOSTRAR ERRORES
            // ==================================

            if (error != null) {

                Text(
                    text = "Error: $error",
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            // ==================================
            // LISTADO DE PRODUCTOS
            // ==================================

            when {

                cargando -> {

                    CircularProgressIndicator()
                }

                productos.isEmpty() && error == null -> {

                    Text("No hay productos registrados.")
                }

                productos.isNotEmpty() -> {

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {

                        items(
                            items = productos,
                            key = { it.id_productos }
                        ) { producto ->

                            ProductoCard(
                                producto = producto,
                                onAgregarFoto = onAgregarFoto,
                                onEditarProducto = onEditarProducto,
                                onEliminarProducto = {
                                    productoAEliminar = it
                                },
                                onAdministrarImagenes = onAdministrarImagenes
                            )
                        }
                    }
                }
            }
        }

    } // Cierre correcto de Scaffold

    // ==========================================
    // CONFIRMACIÓN DE ELIMINACIÓN
    // ==========================================

    productoAEliminar?.let { producto ->

        AlertDialog(
            onDismissRequest = {
                if (!eliminando) {
                    productoAEliminar = null
                }
            },

            title = {
                Text("Eliminar producto")
            },

            text = {
                Text(
                    "¿Estás seguro de eliminar '${producto.nombre}'?\n\n" +
                            "Esta acción no se puede deshacer."
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {
                        if (!eliminando) {
                            eliminarProducto(producto)
                        }
                    },
                    enabled = !eliminando
                ) {

                    if (eliminando) {

                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )

                    } else {

                        Text(
                            "Eliminar",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        productoAEliminar = null
                    },
                    enabled = !eliminando
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

} // Cierre de ProductosScreen


// ==============================================
// TARJETA DE PRODUCTO
// ==============================================

@Composable
fun ProductoCard(
    producto: Producto,
    onAgregarFoto: (Producto) -> Unit,
    onEditarProducto: (Producto) -> Unit,
    onEliminarProducto: (Producto) -> Unit,
    onAdministrarImagenes: (Producto) -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            // Fotografías
            GaleriaProducto(producto = producto)

            Spacer(modifier = Modifier.height(14.dp))

            // Nombre
            Text(
                text = producto.nombre,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(6.dp))

            // SKU
            Text(
                text = "SKU: ${producto.sku}",
                style = MaterialTheme.typography.bodySmall
            )

            // Material
            Text(
                text = "Material: ${producto.material}"
            )

            // Categoría
            Text(
                text = "Categoría: ${producto.categoria?.nombre ?: "Sin categoría"}"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Precio
            Text(
                text = "$" + String.format(
                    Locale.forLanguageTag("es-MX"),
                    "%,.2f",
                    producto.precio_venta.toDoubleOrNull() ?: 0.0
                ) + " MXN",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Inventario
            Text(
                text = producto.inventario?.let {
                    "Stock: ${it.stock_actual}"
                } ?: "Sin inventario"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ==================================
            // BOTÓN AGREGAR FOTOGRAFÍA
            // ==================================

            OutlinedButton(
                onClick = {
                    onAgregarFoto(producto)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("📷 Agregar fotografía")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    onAdministrarImagenes(producto)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🖼️ Administrar fotografías")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ==================================
            // BOTÓN EDITAR
            // ==================================

            Button(
                onClick = {
                    onEditarProducto(producto)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("✏️ Editar producto")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ==================================
            // BOTÓN ELIMINAR
            // ==================================

            OutlinedButton(
                onClick = {
                    onEliminarProducto(producto)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("🗑️ Eliminar producto")
            }
        }
    }
}


// ==============================================
// GALERÍA DE FOTOGRAFÍAS
// ==============================================

@Composable
fun GaleriaProducto(producto: Producto) {

    val context = LocalContext.current

    // Ordenar fotografías:
    // primero la principal y después las demás

    val imagenes = producto.imagenes.sortedWith(
        compareByDescending<ProductoImagen> {
            it.es_principal
        }.thenBy {
            it.orden
        }
    )

    // ==========================================
    // PRODUCTO SIN FOTOGRAFÍAS
    // ==========================================

    if (imagenes.isEmpty()) {

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {

            Box(
                contentAlignment = Alignment.Center
            ) {
                Text("Sin fotografías disponibles")
            }
        }

        return
    }

    // ==========================================
    // CANTIDAD DE FOTOGRAFÍAS
    // ==========================================

    Text(
        text = "${imagenes.size} fotografía(s)",
        style = MaterialTheme.typography.labelMedium
    )

    Spacer(modifier = Modifier.height(8.dp))

    // ==========================================
    // GALERÍA HORIZONTAL
    // ==========================================

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(end = 12.dp)
    ) {

        itemsIndexed(
            items = imagenes,
            key = { _, imagen -> imagen.id_img }
        ) { indice, imagen ->

            val ruta = imagen.url_img

            val rutaLimpia = ruta
                .substringAfter("/storage/", ruta)
                .removePrefix("storage/")
                .removePrefix("public/")
                .trimStart('/')

            // Conexión a Laravel mediante adb reverse
            val imagenUrl =
                "http://127.0.0.1:8000/storage/$rutaLimpia"

            Column(
                modifier = Modifier.width(250.dp)
            ) {

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {

                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imagenUrl)
                            .crossfade(true)
                            .build(),

                        contentDescription =
                            "Fotografía ${indice + 1} de ${producto.nombre}",

                        modifier = Modifier.fillMaxSize(),

                        contentScale = ContentScale.Crop,

                        onError = { resultado ->
                            android.util.Log.e(
                                "JOYERIA_IMAGEN",
                                "Error al cargar $imagenUrl",
                                resultado.result.throwable
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (imagen.es_principal) {
                        "Foto ${indice + 1} · Principal"
                    } else {
                        "Foto ${indice + 1}"
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

