package com.andres.joyeria.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import java.util.Locale

@Composable
fun DetalleProductoScreen(
    token: String,
    producto: Producto,
    onVolver: () -> Unit,
    onEditarProducto: (Producto) -> Unit,
    onAgregarFoto: (Producto) -> Unit,
    onAdministrarImagenes: (Producto) -> Unit,
    onProductoEliminado: () -> Unit,
    onVerInventario: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var mostrarConfirmacionEliminar by remember { mutableStateOf(false) }
    var eliminando by remember { mutableStateOf(false) }

    val imagenes = producto.imagenes.sortedWith(
        compareByDescending<ProductoImagen> { it.es_principal }.thenBy { it.orden }
    )

    fun eliminar() {
        scope.launch {
            eliminando = true
            try {
                val resp = RetrofitClient.api.eliminarProducto("Bearer $token", producto.id_productos)
                if (resp.isSuccessful) {
                    onProductoEliminado()
                } else {
                    snackbarHostState.showSnackbar("Error al eliminar producto")
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Error: ${e.message}")
            } finally {
                eliminando = false
                mostrarConfirmacionEliminar = false
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = JoyeriaFondo
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
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
                        text = "Productos",
                        style = MaterialTheme.typography.bodyLarge,
                        color = JoyeriaOscuro
                    )
                }

                TextButton(
                    onClick = { onEditarProducto(producto) }
                ) {
                    Text(
                        text = "Editar",
                        style = MaterialTheme.typography.bodyLarge,
                        color = JoyeriaDorado,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==================================
            // IMAGEN PRINCIPAL / GALERÍA
            // ==================================
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                shape = RoundedCornerShape(16.dp),
                color = JoyeriaBlancoCampo,
                border = BorderStroke(1.dp, JoyeriaBorde)
            ) {
                if (imagenes.isNotEmpty()) {
                    val imgPrincipal = imagenes.first()
                    val rutaLimpia = imgPrincipal.url_img
                        .substringAfter("/storage/", imgPrincipal.url_img)
                        .removePrefix("storage/")
                        .removePrefix("public/")
                        .trimStart('/')
                    val url = "http://127.0.0.1:8000/storage/$rutaLimpia"

                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(url)
                            .crossfade(true)
                            .build(),
                        contentDescription = producto.nombre,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Text("Sin fotografías disponibles", color = JoyeriaGris)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "★ Fotografía principal",
                    style = MaterialTheme.typography.bodySmall,
                    color = JoyeriaDorado
                )
                Text(
                    text = "${imagenes.size} de ${imagenes.size} fotografía${if (imagenes.size == 1) "" else "s"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = JoyeriaGris
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==================================
            // INFORMACIÓN DEL PRODUCTO
            // ==================================
            Text(
                text = "PRODUCTO #${producto.id_productos} · SKU ${producto.sku}",
                style = MaterialTheme.typography.labelSmall,
                color = JoyeriaGris,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = producto.nombre,
                style = MaterialTheme.typography.headlineLarge,
                color = JoyeriaOscuro,
                fontWeight = FontWeight.Normal
            )

            if (!producto.descripcion.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = producto.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = JoyeriaGris
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==================================
            // PRECIOS
            // ==================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Precio de venta", style = MaterialTheme.typography.bodySmall, color = JoyeriaGris)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$" + String.format(Locale.forLanguageTag("es-MX"), "%,.2f", producto.precio_venta.toDoubleOrNull() ?: 0.0) + " MXN",
                        style = MaterialTheme.typography.titleMedium,
                        color = JoyeriaOscuro,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Precio de costo", style = MaterialTheme.typography.bodySmall, color = JoyeriaGris)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$" + String.format(Locale.forLanguageTag("es-MX"), "%,.2f", producto.precio_costo.toDoubleOrNull() ?: 0.0) + " MXN",
                        style = MaterialTheme.typography.titleMedium,
                        color = JoyeriaOscuro,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==================================
            // CATEGORÍA Y MATERIAL
            // ==================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Categoría", style = MaterialTheme.typography.bodySmall, color = JoyeriaGris)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${producto.categoria?.nombre ?: "Sin categoría"} · ID ${producto.id_categoria}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = JoyeriaOscuro
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Material", style = MaterialTheme.typography.bodySmall, color = JoyeriaGris)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = producto.material.ifBlank { "No especificado" },
                        style = MaterialTheme.typography.bodyLarge,
                        color = JoyeriaOscuro
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==================================
            // PESO Y TALLA
            // ==================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Peso", style = MaterialTheme.typography.bodySmall, color = JoyeriaGris)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (!producto.peso_gr.isNullOrBlank()) "${producto.peso_gr} g" else "No especificado",
                        style = MaterialTheme.typography.bodyLarge,
                        color = JoyeriaOscuro
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Talla / medida", style = MaterialTheme.typography.bodySmall, color = JoyeriaGris)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (!producto.talla_medida.isNullOrBlank()) producto.talla_medida else "No especificada",
                        style = MaterialTheme.typography.bodyLarge,
                        color = JoyeriaOscuro
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==================================
            // ESTADO E INVENTARIO CHIPS
            // ==================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (producto.status) JoyeriaDorado.copy(alpha = 0.15f) else JoyeriaGris.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (producto.status) "Producto activo" else "Producto inactivo",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (producto.status) JoyeriaDorado else JoyeriaGris,
                        fontWeight = FontWeight.Medium
                    )
                }

                val stock = producto.inventario?.stock_actual ?: 0
                Text(
                    text = if (stock > 0) "Stock: $stock" else "Sin inventario",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (stock > 0) JoyeriaOscuro else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = JoyeriaBorde)
            Spacer(modifier = Modifier.height(20.dp))

            // ==================================
            // SECCIÓN: ADMINISTRAR PRODUCTO
            // ==================================
            Text(
                text = "Administrar producto",
                style = MaterialTheme.typography.titleLarge,
                color = JoyeriaOscuro
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botón Editar Producto
            Button(
                onClick = { onEditarProducto(producto) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = JoyeriaOscuro,
                    contentColor = JoyeriaBlancoCampo
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Editar producto")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Ver inventario card/row
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onVerInventario() },
                shape = RoundedCornerShape(12.dp),
                color = JoyeriaBlancoCampo,
                border = BorderStroke(1.dp, JoyeriaBorde)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Inventory, contentDescription = "Inventario", tint = JoyeriaDorado)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Ver inventario",
                            style = MaterialTheme.typography.bodyLarge,
                            color = JoyeriaOscuro
                        )
                    }
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Ir", tint = JoyeriaOscuro)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Agregar fotografía
            OutlinedButton(
                onClick = { onAgregarFoto(producto) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = JoyeriaOscuro),
                border = BorderStroke(1.dp, JoyeriaBorde)
            ) {
                Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = "Agregar foto", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agregar fotografía")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Administrar fotografías
            OutlinedButton(
                onClick = { onAdministrarImagenes(producto) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = JoyeriaOscuro),
                border = BorderStroke(1.dp, JoyeriaBorde)
            ) {
                Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = "Administrar fotos", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Administrar fotografías")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Eliminar producto
            TextButton(
                onClick = { mostrarConfirmacionEliminar = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Eliminar producto", color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (mostrarConfirmacionEliminar) {
        AlertDialog(
            onDismissRequest = { if (!eliminando) mostrarConfirmacionEliminar = false },
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "¿Eliminar producto?",
                        style = MaterialTheme.typography.headlineSmall,
                        color = JoyeriaOscuro,
                        fontWeight = FontWeight.Normal
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(JoyeriaBlancoCampo, RoundedCornerShape(12.dp))
                            .border(1.dp, JoyeriaBorde, RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val imagenes = producto.imagenes
                        if (imagenes.isNotEmpty()) {
                            val imgPrincipal = imagenes.first()
                            val rutaLimpia = imgPrincipal.url_img
                                .substringAfter("/storage/", imgPrincipal.url_img)
                                .removePrefix("storage/")
                                .removePrefix("public/")
                                .trimStart('/')
                            val url = "http://127.0.0.1:8000/storage/$rutaLimpia"

                            Surface(
                                modifier = Modifier.size(48.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current).data(url).crossfade(true).build(),
                                    contentDescription = producto.nombre,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        Column {
                            Text(
                                text = producto.nombre,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = JoyeriaOscuro
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "SKU ${producto.sku}",
                                style = MaterialTheme.typography.bodySmall,
                                color = JoyeriaGris
                            )
                        }
                    }

                    Text(
                        text = "El producto y sus fotografías se eliminarán del catálogo. Esta acción no se puede deshacer.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = JoyeriaGris
                    )
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { eliminar() },
                        enabled = !eliminando,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = JoyeriaBlancoCampo
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (eliminando) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = JoyeriaBlancoCampo)
                        } else {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sí, eliminar producto")
                        }
                    }

                    OutlinedButton(
                        onClick = { mostrarConfirmacionEliminar = false },
                        enabled = !eliminando,
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
}
