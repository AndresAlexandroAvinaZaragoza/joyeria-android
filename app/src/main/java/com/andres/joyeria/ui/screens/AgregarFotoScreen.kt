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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.andres.joyeria.data.api.RetrofitClient
import com.andres.joyeria.ui.theme.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

@Composable
fun AgregarFotoScreen(
    token: String,
    productoId: Int,
    productoNombre: String,
    onVolver: () -> Unit,
    onImagenSubida: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var imagenUri by remember { mutableStateOf<Uri?>(null) }
    var esPrincipal by remember { mutableStateOf(true) }
    var subiendo by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }

    val selectorImagen = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        imagenUri = uri
        mensaje = ""
    }

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
                        text = "Fotografías",
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
                text = "IMAGEN DEL PRODUCTO",
                style = MaterialTheme.typography.labelMedium,
                color = JoyeriaDorado,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Agregar fotografía",
                style = MaterialTheme.typography.headlineLarge,
                color = JoyeriaOscuro,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ==================================
            // TARJETA DE PRODUCTO ASOCIADO
            // ==================================
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = JoyeriaBlancoCampo,
                border = BorderStroke(1.dp, JoyeriaBorde)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = JoyeriaFondo
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Producto",
                                tint = JoyeriaDorado,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = productoNombre,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = JoyeriaOscuro
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Producto #$productoId",
                            style = MaterialTheme.typography.bodySmall,
                            color = JoyeriaGris
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ==================================
            // SELECCIÓN DE IMAGEN (ÁREA PUNTEADA)
            // ==================================
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = JoyeriaBlancoCampo,
                border = BorderStroke(1.dp, JoyeriaBorde)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Subir",
                        tint = JoyeriaDorado,
                        modifier = Modifier.size(40.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Selecciona una imagen de la pieza",
                        style = MaterialTheme.typography.bodyMedium,
                        color = JoyeriaOscuro,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = { selectorImagen.launch("image/*") },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = JoyeriaOscuro),
                        border = BorderStroke(1.dp, JoyeriaBorde)
                    ) {
                        Text(
                            text = if (imagenUri == null) "Seleccionar imagen" else "Cambiar imagen",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (imagenUri == null) "Ningún archivo seleccionado" else obtenerNombreArchivo(context, imagenUri!!),
                        style = MaterialTheme.typography.bodySmall,
                        color = JoyeriaGris
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ==================================
            // SWITCH IMAGEN PRINCIPAL
            // ==================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Imagen principal", style = MaterialTheme.typography.bodyLarge, color = JoyeriaOscuro, fontWeight = FontWeight.Medium)
                    Text(text = "Se mostrará primero en la ficha del producto.", style = MaterialTheme.typography.bodySmall, color = JoyeriaGris)
                }
                Switch(
                    checked = esPrincipal,
                    onCheckedChange = { esPrincipal = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = JoyeriaDorado)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "La nueva fotografía se añadirá a la galería de $productoNombre.",
                style = MaterialTheme.typography.bodySmall,
                color = JoyeriaGris
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
                    val uri = imagenUri
                    if (uri == null) {
                        mensaje = "Selecciona una imagen."
                        return@Button
                    }

                    subiendo = true
                    mensaje = ""

                    scope.launch {
                        try {
                            val bytes = context.contentResolver
                                .openInputStream(uri)
                                ?.use { it.readBytes() }

                            if (bytes == null) {
                                mensaje = "No se pudo leer la imagen."
                                return@launch
                            }

                            val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                            val requestBody = bytes.toRequestBody(mimeType.toMediaType())
                            val imagenPart = MultipartBody.Part.createFormData(
                                "imagen",
                                obtenerNombreArchivo(context, uri),
                                requestBody
                            )

                            val principalBody = (if (esPrincipal) "1" else "0").toRequestBody("text/plain".toMediaType())

                            RetrofitClient.api.subirImagenProducto(
                                authorization = "Bearer $token",
                                productoId = productoId,
                                imagen = imagenPart,
                                esPrincipal = principalBody
                            )

                            onImagenSubida()
                        } catch (e: Exception) {
                            mensaje = "Error al subir: ${e.message}"
                            e.printStackTrace()
                        } finally {
                            subiendo = false
                        }
                    }
                },
                enabled = !subiendo,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = JoyeriaOscuro,
                    contentColor = JoyeriaBlancoCampo
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (subiendo) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = JoyeriaBlancoCampo)
                } else {
                    Icon(imageVector = Icons.Default.Upload, contentDescription = "Subir", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Subir fotografía", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                }
            }

            OutlinedButton(
                onClick = onVolver,
                enabled = !subiendo,
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

private fun obtenerNombreArchivo(context: Context, uri: Uri): String {
    var nombre = "imagen.jpg"
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        val indice = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (indice >= 0 && it.moveToFirst()) {
            nombre = it.getString(indice) ?: "imagen.jpg"
        }
    }
    return nombre
}
