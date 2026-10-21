package com.andres.joyeria.ui.screens

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.andres.joyeria.data.api.Producto
import com.andres.joyeria.data.api.ProductoImagen
import com.andres.joyeria.data.api.RetrofitClient
import com.andres.joyeria.ui.theme.*
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

    var imagenEditando by remember { mutableStateOf<ProductoImagen?>(null) }
    var imagenEliminando by remember { mutableStateOf<ProductoImagen?>(null) }

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
                val respuesta = RetrofitClient.api.obtenerProductos("Bearer $token")
                val actualizado = respuesta.productos.firstOrNull { it.id_productos == producto.id_productos }
                if (actualizado != null) {
                    imagenes = actualizado.imagenes
                }
            } catch (e: Exception) {
                snackbar.showSnackbar("No se pudieron actualizar las imágenes: ${e.message}")
            }
        }
    }

    LaunchedEffect(producto.id_productos) {
        recargarImagenes()
    }

    fun guardarCambios() {
        val seleccionada = imagenEditando ?: return
        val numeroOrden = orden.toIntOrNull() ?: 0

        scope.launch {
            procesando = true
            try {
                val imagenPart = nuevaUri?.let { crearParteImagen(context, it) }
                val principalBody = (if (esPrincipal) "1" else "0").toRequestBody("text/plain".toMediaType())
                val ordenBody = numeroOrden.toString().toRequestBody("text/plain".toMediaType())

                val respuesta = RetrofitClient.api.actualizarImagenProducto(
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
                    snackbar.showSnackbar("Fotografía actualizada correctamente")
                } else {
                    snackbar.showSnackbar("Error al actualizar. Código: ${respuesta.code()}")
                }
            } catch (e: Exception) {
                snackbar.showSnackbar("Error: ${e.message}")
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
                val respuesta = RetrofitClient.api.eliminarImagenProducto(
                    authorization = "Bearer $token",
                    productoId = producto.id_productos,
                    imagenId = seleccionada.id_img
                )
                if (respuesta.isSuccessful) {
                    imagenEliminando = null
                    recargarImagenes()
                    snackbar.showSnackbar("Fotografía eliminada correctamente")
                } else {
                    snackbar.showSnackbar("Error al eliminar. Código: ${respuesta.code()}")
                }
            } catch (e: Exception) {
                snackbar.showSnackbar("Error: ${e.message}")
            } finally {
                procesando = false
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbar) },
        containerColor = JoyeriaFondo
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
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

                Icon(
                    imageVector = Icons.Default.Diamond,
                    contentDescription = "Logo",
                    tint = JoyeriaDorado,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==================================
            // HEADER TÍTULO
            // ==================================
            Text(
                text = producto.nombre.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = JoyeriaDorado,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Fotografías",
                style = MaterialTheme.typography.headlineLarge,
                color = JoyeriaOscuro,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${imagenes.size} fotografía${if (imagenes.size == 1) "" else "s"} · SKU ${producto.sku}",
                style = MaterialTheme.typography.bodyMedium,
                color = JoyeriaGris
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ==================================
            // BOTÓN AGREGAR FOTOGRAFÍA
            // ==================================
            Button(
                onClick = { selectorImagen.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = JoyeriaOscuro,
                    contentColor = JoyeriaBlancoCampo
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Agregar", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agregar fotografía", style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ==================================
            // GRID DE FOTOGRAFÍAS (2 columnas como en la captura)
            // ==================================
            if (imagenes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Este producto no tiene fotografías.", color = JoyeriaGris)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(
                        items = imagenes.sortedWith(
                            compareByDescending<ProductoImagen> { it.es_principal }.thenBy { it.orden }
                        ),
                        key = { it.id_img }
                    ) { imagen ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = JoyeriaBlancoCampo),
                            border = BorderStroke(1.dp, JoyeriaBorde),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp)
                            ) {
                                val ruta = imagen.url_img
                                    .substringAfter("/storage/", imagen.url_img)
                                    .removePrefix("storage/")
                                    .removePrefix("public/")
                                    .trimStart('/')
                                val url = "http://127.0.0.1:8000/storage/$ruta"

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    color = JoyeriaBlancoCampo
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(url)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Foto producto",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Orden ${imagen.orden}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = JoyeriaGris
                                    )
                                    if (imagen.es_principal) {
                                        Text(
                                            text = "★ Principal",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = JoyeriaDorado,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    TextButton(
                                        onClick = {
                                            imagenEditando = imagen
                                            orden = imagen.orden.toString()
                                            esPrincipal = imagen.es_principal
                                            nuevaUri = null
                                        },
                                        contentPadding = PaddingValues(0.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(14.dp), tint = JoyeriaOscuro)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Editar", style = MaterialTheme.typography.bodySmall, color = JoyeriaOscuro)
                                    }

                                    IconButton(
                                        onClick = { imagenEliminando = imagen },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ==================================
            // INFO FOOTER
            // ==================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(JoyeriaDorado.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = JoyeriaDorado,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "En Editar puedes cambiar el orden, elegir la imagen principal o reemplazar la fotografía.",
                    style = MaterialTheme.typography.bodySmall,
                    color = JoyeriaOscuro
                )
            }
        }
    }

    // ==========================================
    // DIÁLOGO EDITAR FOTOGRAFÍA
    // ==========================================
    imagenEditando?.let { imagen ->
        AlertDialog(
            onDismissRequest = { if (!procesando) imagenEditando = null },
            title = {
                Column {
                    Text(
                        text = "${producto.nombre.uppercase()} - FOTOGRAFÍA #${imagen.id_img}",
                        style = MaterialTheme.typography.labelSmall,
                        color = JoyeriaDorado
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Editar fotografía",
                        style = MaterialTheme.typography.headlineSmall,
                        color = JoyeriaOscuro
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Preview actual
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(JoyeriaBlancoCampo, RoundedCornerShape(12.dp))
                            .border(1.dp, JoyeriaBorde, RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val ruta = imagen.url_img
                            .substringAfter("/storage/", imagen.url_img)
                            .removePrefix("storage/")
                            .removePrefix("public/")
                            .trimStart('/')
                        val url = "http://127.0.0.1:8000/storage/$ruta"

                        Surface(
                            modifier = Modifier.size(56.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context).data(url).crossfade(true).build(),
                                contentDescription = "Actual",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Fotografía actual",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = JoyeriaOscuro
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Orden ${imagen.orden} · ${if (imagen.es_principal) "Es principal" else "No es principal"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = JoyeriaGris
                            )
                        }
                    }

                    OutlinedTextField(
                        value = orden,
                        onValueChange = { orden = it },
                        label = { Text("Orden de visualización") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JoyeriaDorado,
                            unfocusedBorderColor = JoyeriaBorde,
                            focusedContainerColor = JoyeriaBlancoCampo,
                            unfocusedContainerColor = JoyeriaBlancoCampo
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Imagen principal", style = MaterialTheme.typography.bodyLarge, color = JoyeriaOscuro)
                            Text(text = "Usar como primera imagen del producto.", style = MaterialTheme.typography.bodySmall, color = JoyeriaGris)
                        }
                        Switch(
                            checked = esPrincipal,
                            onCheckedChange = { esPrincipal = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = JoyeriaDorado)
                        )
                    }

                    OutlinedButton(
                        onClick = { selectorImagen.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = JoyeriaOscuro),
                        border = BorderStroke(1.dp, JoyeriaBorde)
                    ) {
                        Icon(imageVector = Icons.Default.Image, contentDescription = "Imagen", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (nuevaUri == null) "Reemplazar fotografía" else "Fotografía seleccionada"
                        )
                    }
                    if (nuevaUri != null) {
                        Text(
                            text = "Nueva imagen seleccionada lista para guardar",
                            style = MaterialTheme.typography.bodySmall,
                            color = JoyeriaDorado
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
                        onClick = { guardarCambios() },
                        enabled = !procesando,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = JoyeriaOscuro, contentColor = JoyeriaBlancoCampo),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (procesando) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = JoyeriaBlancoCampo)
                        } else {
                            Icon(imageVector = Icons.Default.Check, contentDescription = "Guardar", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Guardar cambios")
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            imagenEditando = null
                            nuevaUri = null
                        },
                        enabled = !procesando,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = JoyeriaOscuro),
                        border = BorderStroke(1.dp, JoyeriaBorde)
                    ) {
                        Text("Cancelar")
                    }
                }
            }
        )
    }

    // ==========================================
    // DIÁLOGO ELIMINAR FOTOGRAFÍA
    // ==========================================
    imagenEliminando?.let {
        AlertDialog(
            onDismissRequest = { if (!procesando) imagenEliminando = null },
            title = { Text("Eliminar fotografía") },
            text = { Text("¿Deseas eliminar esta fotografía? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = { eliminarImagen() },
                    enabled = !procesando
                ) {
                    if (procesando) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Eliminar", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { imagenEliminando = null },
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

    val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
    var nombre = "imagen.jpg"

    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val indice = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (indice >= 0 && cursor.moveToFirst()) {
            nombre = cursor.getString(indice) ?: nombre
        }
    }

    val body = bytes.toRequestBody(mime.toMediaType())
    return MultipartBody.Part.createFormData("imagen", nombre, body)
}
