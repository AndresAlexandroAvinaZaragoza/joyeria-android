
package com.andres.joyeria.ui.screens

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.andres.joyeria.data.api.Producto
import com.andres.joyeria.data.api.ProductoImagen
import com.andres.joyeria.data.api.RetrofitClient
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

@Composable
fun AdministrarImagenesScreen(
    token: String,
    producto: Producto,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    var imagenes by remember(producto.id_productos) {
        mutableStateOf(producto.imagenes)
    }

    var imagenEditando by remember {
        mutableStateOf<ProductoImagen?>(null)
    }

    var imagenEliminando by remember {
        mutableStateOf<ProductoImagen?>(null)
    }

    var orden by remember { mutableStateOf("0") }
    var esPrincipal by remember { mutableStateOf(false) }
    var nuevaUri by remember { mutableStateOf<Uri?>(null) }
    var procesando by remember { mutableStateOf(false) }

    val selectorImagen = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        nuevaUri = uri
    }

    fun recargarImagenes() {
        scope.launch {
            try {
                val respuesta = RetrofitClient.api.obtenerProductos(
                    "Bearer $token"
                )

                val actualizado = respuesta.productos.firstOrNull {
                    it.id_productos == producto.id_productos
                }

                if (actualizado != null) {
                    imagenes = actualizado.imagenes
                }
            } catch (e: Exception) {
                snackbar.showSnackbar(
                    "No se pudieron actualizar las imágenes: ${e.message}"
                )
            }
        }
    }

    LaunchedEffect(producto.id_productos) {
        recargarImagenes()
    }

    fun guardarCambios() {
        val seleccionada = imagenEditando ?: return

        val numeroOrden = orden.toIntOrNull()

        if (numeroOrden == null || numeroOrden < 0) {
            scope.launch {
                snackbar.showSnackbar("Ingresa un orden válido (0 o mayor)")
            }
            return
        }

        scope.launch {
            procesando = true

            try {
                val imagenPart = nuevaUri?.let { uri ->
                    crearParteImagen(context, uri)
                }

                val principalBody = (
                        if (esPrincipal) "1" else "0"
                        ).toRequestBody("text/plain".toMediaType())

                val ordenBody = numeroOrden.toString()
                    .toRequestBody("text/plain".toMediaType())

                val respuesta =
                    RetrofitClient.api.actualizarImagenProducto(
                        authorization = "Bearer $token",
                        productoId = producto.id_productos,
                        imagenId = seleccionada.id_img,
                        imagen = imagenPart,
                        esPrincipal = principalBody,
                        orden = ordenBody
                    )

                if (respuesta.isSuccessful) {
                    imagenEditando = null
                    nuevaUri = null

                    recargarImagenes()

                    snackbar.showSnackbar(
                        "Fotografía actualizada correctamente"
                    )
                } else {
                    snackbar.showSnackbar(
                        "Error al actualizar. Código: ${respuesta.code()}"
                    )
                }

            } catch (e: Exception) {
                snackbar.showSnackbar(
                    "Error: ${e.message}"
                )
            } finally {
                procesando = false
            }
        }
    }

    fun eliminarImagen() {
        val seleccionada = imagenEliminando ?: return

        scope.launch {
            procesando = true

            try {
                val respuesta =
                    RetrofitClient.api.eliminarImagenProducto(
                        authorization = "Bearer $token",
                        productoId = producto.id_productos,
                        imagenId = seleccionada.id_img
                    )

                if (respuesta.isSuccessful) {
                    imagenEliminando = null

                    recargarImagenes()

                    snackbar.showSnackbar(
                        "Fotografía eliminada correctamente"
                    )
                } else {
                    snackbar.showSnackbar(
                        "Error al eliminar. Código: ${respuesta.code()}"
                    )
                }

            } catch (e: Exception) {
                snackbar.showSnackbar(
                    "Error: ${e.message}"
                )
            } finally {
                procesando = false
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbar)
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Administrar fotografías",
                    style = MaterialTheme.typography.titleLarge
                )

                TextButton(onClick = onVolver) {
                    Text("Volver")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                producto.nombre,
                style = MaterialTheme.typography.titleMedium
            )

            Text("${imagenes.size} fotografía(s)")

            Spacer(modifier = Modifier.height(16.dp))

            if (imagenes.isEmpty()) {
                Text("Este producto no tiene fotografías.")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(
                        items = imagenes.sortedWith(
                            compareByDescending<ProductoImagen> {
                                it.es_principal
                            }.thenBy {
                                it.orden
                            }
                        ),
                        key = { it.id_img }
                    ) { imagen ->

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                val ruta = imagen.url_img
                                    .substringAfter(
                                        "/storage/",
                                        imagen.url_img
                                    )
                                    .removePrefix("storage/")
                                    .removePrefix("public/")
                                    .trimStart('/')

                                val url =
                                    "http://127.0.0.1:8000/storage/$ruta"

                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(url)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Fotografía del producto",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp),
                                    contentScale = ContentScale.Crop
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Text("Orden: ${imagen.orden}")

                                if (imagen.es_principal) {
                                    Text(
                                        "★ Fotografía principal",
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    horizontalArrangement =
                                        Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            imagenEditando = imagen
                                            orden = imagen.orden.toString()
                                            esPrincipal = imagen.es_principal
                                            nuevaUri = null
                                        },
                                        modifier = Modifier.weight(1f),
                                        enabled = !procesando
                                    ) {
                                        Text("Editar")
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            imagenEliminando = imagen
                                        },
                                        modifier = Modifier.weight(1f),
                                        enabled = !procesando
                                    ) {
                                        Text("Eliminar")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    imagenEditando?.let { imagen ->

        AlertDialog(
            onDismissRequest = {
                if (!procesando) {
                    imagenEditando = null
                }
            },
            title = {
                Text("Editar fotografía")
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Text("Fotografía #${imagen.id_img}")

                    OutlinedTextField(
                        value = orden,
                        onValueChange = { orden = it },
                        label = { Text("Orden de visualización") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement =
                            Arrangement.SpaceBetween
                    ) {
                        Text("Imagen principal")

                        Switch(
                            checked = esPrincipal,
                            onCheckedChange = {
                                esPrincipal = it
                            }
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            selectorImagen.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (nuevaUri == null) {
                                "Reemplazar fotografía (opcional)"
                            } else {
                                "Cambiar fotografía seleccionada"
                            }
                        )
                    }

                    if (nuevaUri != null) {
                        Text("Nueva fotografía seleccionada")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { guardarCambios() },
                    enabled = !procesando
                ) {
                    Text(
                        if (procesando) "Guardando..." else "Guardar"
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        imagenEditando = null
                        nuevaUri = null
                    },
                    enabled = !procesando
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    imagenEliminando?.let {

        AlertDialog(
            onDismissRequest = {
                if (!procesando) {
                    imagenEliminando = null
                }
            },
            title = {
                Text("Eliminar fotografía")
            },
            text = {
                Text(
                    "¿Deseas eliminar esta fotografía? " +
                            "Esta acción no se puede deshacer."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { eliminarImagen() },
                    enabled = !procesando
                ) {
                    Text(
                        if (procesando) "Eliminando..." else "Eliminar"
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        imagenEliminando = null
                    },
                    enabled = !procesando
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

private fun crearParteImagen(
    context: Context,
    uri: Uri
): MultipartBody.Part {

    val bytes = context.contentResolver
        .openInputStream(uri)
        ?.use { it.readBytes() }
        ?: throw IllegalStateException("No se pudo leer la imagen")

    val mime = context.contentResolver.getType(uri)
        ?: "image/jpeg"

    var nombre = "imagen.jpg"

    context.contentResolver.query(
        uri, null, null, null, null
    )?.use { cursor ->
        val indice = cursor.getColumnIndex(
            OpenableColumns.DISPLAY_NAME
        )

        if (indice >= 0 && cursor.moveToFirst()) {
            nombre = cursor.getString(indice) ?: nombre
        }
    }

    val body = bytes.toRequestBody(mime.toMediaType())

    return MultipartBody.Part.createFormData(
        "imagen",
        nombre,
        body
    )
}
