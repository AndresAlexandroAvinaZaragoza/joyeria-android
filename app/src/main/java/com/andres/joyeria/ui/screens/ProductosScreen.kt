package com.andres.joyeria.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
fun ProductosScreen(
    token: String,
    onVolver: () -> Unit,
    onAgregarProducto: () -> Unit,
    onAgregarFoto: (Producto) -> Unit,
    onEditarProducto: (Producto) -> Unit,
    onAdministrarImagenes: (Producto) -> Unit,
    onVerDetalle: (Producto) -> Unit,
    onNavigateTab: (String) -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var productos by remember { mutableStateOf<List<Producto>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var productoAEliminar by remember { mutableStateOf<Producto?>(null) }
    var eliminando by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf("Todos") }

    LaunchedEffect(token) {
        cargando = true
        error = null
        try {
            val respuesta = RetrofitClient.api.obtenerProductos("Bearer $token")
            productos = respuesta.productos
        } catch (e: Exception) {
            error = e.message ?: "Error al obtener productos"
        } finally {
            cargando = false
        }
    }

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
                    productos = productos.filter { it.id_productos != producto.id_productos }
                    productoAEliminar = null
                    snackbarHostState.showSnackbar(
                        message = "Producto '${producto.nombre}' eliminado correctamente",
                        duration = SnackbarDuration.Short
                    )
                } else {
                    error = "No se pudo eliminar el producto. Código: ${respuesta.code()}"
                    snackbarHostState.showSnackbar(message = error ?: "Error al eliminar producto")
                }
            } catch (e: Exception) {
                error = e.message ?: "Error al conectar con Laravel"
                snackbarHostState.showSnackbar(message = "Error: $error")
            } finally {
                eliminando = false
            }
        }
    }

    val categorias = remember(productos) {
        listOf("Todos") + productos.mapNotNull { it.categoria?.nombre }.distinct()
    }

    val productosFiltrados = remember(productos, searchQuery, categoriaSeleccionada) {
        productos.filter { producto ->
            val matchBusqueda = producto.nombre.contains(searchQuery, ignoreCase = true) ||
                    producto.sku.contains(searchQuery, ignoreCase = true)
            val matchCategoria = categoriaSeleccionada == "Todos" ||
                    producto.categoria?.nombre.equals(categoriaSeleccionada, ignoreCase = true)
            matchBusqueda && matchCategoria
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            JoyeriaBottomBar(
                currentTab = "productos",
                onTabSelected = { tab ->
                    if (tab == "inicio") onVolver()
                    else onNavigateTab(tab)
                }
            )
        },
        containerColor = JoyeriaFondo
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // ==================================
            // ENCABEZADO SUPERIOR (Volver + Diamante)
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
                        text = "Inicio",
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
            // TÍTULO SECCIÓN
            // ==================================
            Text(
                text = "CATÁLOGOS E INVENTARIO",
                style = MaterialTheme.typography.labelMedium,
                color = JoyeriaDorado,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Productos",
                style = MaterialTheme.typography.headlineLarge,
                color = JoyeriaOscuro,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ==================================
            // BOTÓN NUEVO PRODUCTO
            // ==================================
            Button(
                onClick = onAgregarProducto,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = JoyeriaOscuro,
                    contentColor = JoyeriaBlancoCampo
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "+ Nuevo producto",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==================================
            // BARRA DE BÚSQUEDA
            // ==================================
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text("Buscar por nombre o SKU", color = JoyeriaGris)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = JoyeriaGris
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Filtros",
                        tint = JoyeriaGris
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = JoyeriaDorado,
                    unfocusedBorderColor = JoyeriaBorde,
                    focusedContainerColor = JoyeriaBlancoCampo,
                    unfocusedContainerColor = JoyeriaBlancoCampo
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ==================================
            // CHIPS DE CATEGORÍAS
            // ==================================
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(categorias) { categoria ->
                    val isSelected = categoria == categoriaSeleccionada
                    Surface(
                        onClick = { categoriaSeleccionada = categoria },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) JoyeriaOscuro else JoyeriaBlancoCampo,
                        border = if (isSelected) null else BorderStroke(1.dp, JoyeriaBorde),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = categoria,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) JoyeriaBlancoCampo else JoyeriaOscuro,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = JoyeriaOscuro)
                    }
                }

                productosFiltrados.isEmpty() && error == null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontraron productos.",
                            color = JoyeriaGris,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(
                            items = productosFiltrados,
                            key = { it.id_productos }
                        ) { producto ->
                            ProductoCard(
                                producto = producto,
                                onAgregarFoto = onAgregarFoto,
                                onEditarProducto = onEditarProducto,
                                onEliminarProducto = { productoAEliminar = it },
                                onAdministrarImagenes = onAdministrarImagenes,
                                onVerDetalle = onVerDetalle
                            )
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // CONFIRMACIÓN DE ELIMINACIÓN
    // ==========================================
    productoAEliminar?.let { producto ->
        AlertDialog(
            onDismissRequest = {
                if (!eliminando) productoAEliminar = null
            },
            title = { Text("Eliminar producto") },
            text = {
                Text("¿Estás seguro de eliminar '${producto.nombre}'?\n\nEsta acción no se puede deshacer.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (!eliminando) eliminarProducto(producto)
                    },
                    enabled = !eliminando
                ) {
                    if (eliminando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Eliminar", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { productoAEliminar = null },
                    enabled = !eliminando
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// ==============================================
// TARJETA DE PRODUCTO
// ==============================================

@Composable
fun ProductoCard(
    producto: Producto,
    onAgregarFoto: (Producto) -> Unit,
    onEditarProducto: (Producto) -> Unit,
    onEliminarProducto: (Producto) -> Unit,
    onAdministrarImagenes: (Producto) -> Unit,
    onVerDetalle: (Producto) -> Unit
) {
    var expandedMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onVerDetalle(producto) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = JoyeriaBlancoCampo),
        border = BorderStroke(1.dp, JoyeriaBorde),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Imagen del producto
            GaleriaProductoCard(producto = producto)

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = producto.nombre,
                        style = MaterialTheme.typography.titleLarge,
                        color = JoyeriaOscuro,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    val materialStr = if (producto.material.isNotBlank()) " · ${producto.material}" else ""
                    val catStr = if (producto.categoria?.nombre != null) " · ${producto.categoria.nombre}" else ""
                    Text(
                        text = "SKU ${producto.sku}$materialStr$catStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = JoyeriaGris
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "$" + String.format(
                            Locale.forLanguageTag("es-MX"),
                            "%,.2f",
                            producto.precio_venta.toDoubleOrNull() ?: 0.0
                        ) + " MXN",
                        style = MaterialTheme.typography.titleMedium,
                        color = JoyeriaOscuro,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Box {
                    IconButton(onClick = { expandedMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Opciones",
                            tint = JoyeriaOscuro
                        )
                    }

                    DropdownMenu(
                        expanded = expandedMenu,
                        onDismissRequest = { expandedMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Editar producto") },
                            onClick = {
                                expandedMenu = false
                                onEditarProducto(producto)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Agregar foto") },
                            onClick = {
                                expandedMenu = false
                                onAgregarFoto(producto)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Administrar fotos") },
                            onClick = {
                                expandedMenu = false
                                onAdministrarImagenes(producto)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                expandedMenu = false
                                onEliminarProducto(producto)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = JoyeriaBorde, thickness = 1.dp)

            Spacer(modifier = Modifier.height(12.dp))

            // Footer stock & fotos count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val stockActual = producto.inventario?.stock_actual ?: 0
                Text(
                    text = if (stockActual > 0) "Stock: $stockActual" else "Sin inventario",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (stockActual > 0) JoyeriaOscuro else MaterialTheme.colorScheme.error
                )

                Text(
                    text = "${producto.imagenes.size} fotografía${if (producto.imagenes.size == 1) "" else "s"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = JoyeriaGris
                )
            }
        }
    }
}

// ==============================================
// IMAGEN / GALERÍA EN TARJETA
// ==============================================

@Composable
fun GaleriaProductoCard(producto: Producto) {
    val context = LocalContext.current
    val imagenes = producto.imagenes.sortedWith(
        compareByDescending<ProductoImagen> { it.es_principal }.thenBy { it.orden }
    )

    if (imagenes.isEmpty()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(12.dp),
            color = JoyeriaBlancoCampo,
            border = BorderStroke(1.dp, JoyeriaBorde)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "Sin fotografías disponibles",
                    color = JoyeriaGris,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        return
    }

    val imagenPrincipal = imagenes.first()
    val ruta = imagenPrincipal.url_img
    val rutaLimpia = ruta
        .substringAfter("/storage/", ruta)
        .removePrefix("storage/")
        .removePrefix("public/")
        .trimStart('/')

    val imagenUrl = "http://127.0.0.1:8000/storage/$rutaLimpia"

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(12.dp),
        color = JoyeriaBlancoCampo
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(imagenUrl)
                .crossfade(true)
                .build(),
            contentDescription = producto.nombre,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

// ==============================================
// BARRA DE NAVEGACIÓN INFERIOR (BOTTOM BAR)
// ==============================================

@Composable
fun JoyeriaBottomBar(
    currentTab: String,
    onTabSelected: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = JoyeriaBlancoCampo,
        tonalElevation = 8.dp,
        border = BorderStroke(1.dp, JoyeriaBorde)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomBarItem(
                icon = Icons.Default.Diamond,
                label = "Productos",
                isSelected = currentTab == "productos",
                onClick = { onTabSelected("productos") }
            )
            BottomBarItem(
                icon = Icons.Default.PhotoLibrary,
                label = "Fotografías",
                isSelected = currentTab == "fotografias",
                onClick = { onTabSelected("productos") }
            )
            BottomBarItem(
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                label = "Pedidos",
                isSelected = currentTab == "pedidos",
                onClick = { onTabSelected("pedidos") }
            )
            BottomBarItem(
                icon = Icons.Default.LocalShipping,
                label = "Envíos",
                isSelected = currentTab == "envios",
                onClick = { onTabSelected("envios") }
            )
        }
    }
}

@Composable
fun BottomBarItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = if (isSelected) JoyeriaOscuro else JoyeriaGris

    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
        if (isSelected) {
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .height(2.dp)
                    .background(JoyeriaDorado, RoundedCornerShape(1.dp))
            )
        }
    }
}
