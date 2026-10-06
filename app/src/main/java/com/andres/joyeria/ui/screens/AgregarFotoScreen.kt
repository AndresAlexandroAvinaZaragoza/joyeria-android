package com.andres.joyeria.ui.screens

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.andres.joyeria.data.api.RetrofitClient
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody

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

    var imagenUri by remember {
        mutableStateOf<Uri?>(null)
    }

    var esPrincipal by remember {
        mutableStateOf(true)
    }

    var subiendo by remember {
        mutableStateOf(false)
    }

    var mensaje by remember {
        mutableStateOf("")
    }

    val selectorImagen = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->

        imagenUri = uri
        mensaje = ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "Agregar fotografía",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = productoNombre,
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "Producto #$productoId",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(30.dp))

        OutlinedButton(
            onClick = {
                selectorImagen.launch("image/*")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (imagenUri == null) {
                    "Seleccionar imagen"
                } else {
                    "Cambiar imagen"
                }
            )
        }

        if (imagenUri != null) {

            Spacer(modifier = Modifier.height(12.dp))

            Text("Imagen seleccionada correctamente.")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text("Imagen principal")

            Switch(
                checked = esPrincipal,
                onCheckedChange = {
                    esPrincipal = it
                }
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

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
                            ?.use {
                                it.readBytes()
                            }

                        if (bytes == null) {
                            mensaje = "No se pudo leer la imagen."
                            subiendo = false
                            return@launch
                        }

                        val mimeType =
                            context.contentResolver.getType(uri)
                                ?: "image/jpeg"

                        val mediaType =
                            MediaType.parse(mimeType)

                        val requestBody =
                            RequestBody.create(
                                mediaType,
                                bytes
                            )

                        val imagenPart =
                            MultipartBody.Part.createFormData(
                                "imagen",
                                obtenerNombreArchivo(
                                    context,
                                    uri
                                ),
                                requestBody
                            )

                        val principalBody =
                            if (esPrincipal) "1" else "0"

                        val principalRequest =
                            RequestBody.create(
                                MediaType.parse("text/plain"),
                                principalBody
                            )

                        RetrofitClient.api.subirImagenProducto(
                            authorization = "Bearer $token",
                            productoId = productoId,
                            imagen = imagenPart,
                            esPrincipal = principalRequest
                        )

                        onImagenSubida()

                    } catch (e: Exception) {

                        mensaje =
                            "Error al subir: ${e.message}"

                        e.printStackTrace()

                    } finally {

                        subiendo = false
                    }
                }
            },

            enabled = !subiendo,

            modifier = Modifier.fillMaxWidth()
        ) {

            if (subiendo) {

                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )

            } else {

                Text("Subir fotografía")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancelar")
        }

        if (mensaje.isNotBlank()) {

            Spacer(modifier = Modifier.height(12.dp))

            Text(mensaje)
        }
    }
}

private fun obtenerNombreArchivo(
    context: Context,
    uri: Uri
): String {

    var nombre = "imagen.jpg"

    val cursor = context.contentResolver.query(
        uri,
        null,
        null,
        null,
        null
    )

    cursor?.use {

        val indice =
            it.getColumnIndex(
                OpenableColumns.DISPLAY_NAME
            )

        if (indice >= 0 && it.moveToFirst()) {
            nombre = it.getString(indice)
        }
    }

    return nombre
}