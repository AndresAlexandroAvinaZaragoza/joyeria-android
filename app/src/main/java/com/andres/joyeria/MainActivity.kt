
package com.andres.joyeria

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*

import com.andres.joyeria.data.api.Producto

import com.andres.joyeria.ui.screens.HomeScreen
import com.andres.joyeria.ui.screens.LoginScreen
import com.andres.joyeria.ui.screens.ProductosScreen
import com.andres.joyeria.ui.screens.AgregarProductoScreen
import com.andres.joyeria.ui.screens.AgregarFotoScreen
import com.andres.joyeria.ui.screens.EditarProductoScreen
import com.andres.joyeria.ui.screens.AdministrarImagenesScreen

import com.andres.joyeria.ui.theme.JoyeriaAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            JoyeriaAppTheme {

                // ==========================================
                // VARIABLES DE ESTADO
                // ==========================================

                var token by remember {
                    mutableStateOf<String?>(null)
                }

                var nombre by remember {
                    mutableStateOf("")
                }

                var pantalla by remember {
                    mutableStateOf("home")
                }

                var productoSeleccionado by remember {
                    mutableStateOf<Producto?>(null)
                }

                // ==========================================
                // LOGIN
                // ==========================================

                if (token == null) {

                    LoginScreen(
                        onLoginSuccess = { nuevoToken, nuevoNombre ->
                            token = nuevoToken
                            nombre = nuevoNombre
                            pantalla = "home"
                        }
                    )

                } else {

                    // ======================================
                    // PANTALLA PRINCIPAL
                    // ======================================

                    if (pantalla == "home") {

                        HomeScreen(
                            nombre = nombre,

                            onProductosClick = {
                                pantalla = "productos"
                            },

                            onFotografiasClick = {
                                pantalla = "productos"
                            },

                            onCerrarSesion = {
                                token = null
                                nombre = ""
                                productoSeleccionado = null
                                pantalla = "home"
                            }
                        )

                        // ======================================
                        // LISTA DE PRODUCTOS
                        // ======================================

                    } else if (pantalla == "productos") {

                        ProductosScreen(
                            token = token!!,

                            onVolver = {
                                pantalla = "home"
                            },

                            onAgregarProducto = {
                                pantalla = "agregar_producto"
                            },

                            onAgregarFoto = { producto ->
                                productoSeleccionado = producto
                                pantalla = "agregar_foto"
                            },

                            onEditarProducto = { producto ->
                                productoSeleccionado = producto
                                pantalla = "editar_producto"
                            },

                            onAdministrarImagenes = { producto ->
                                productoSeleccionado = producto
                                pantalla = "administrar_imagenes"
                            }
                        )

                        // ======================================
                        // AGREGAR PRODUCTO
                        // ======================================

                    } else if (pantalla == "agregar_producto") {

                        AgregarProductoScreen(
                            token = token!!,

                            onVolver = {
                                pantalla = "productos"
                            },

                            onProductoCreado = {
                                pantalla = "productos"
                            }
                        )

                        // ======================================
                        // AGREGAR FOTOGRAFÍA
                        // ======================================

                    } else if (pantalla == "agregar_foto") {

                        val producto = productoSeleccionado

                        if (producto != null) {

                            AgregarFotoScreen(
                                token = token!!,
                                productoId = producto.id_productos,
                                productoNombre = producto.nombre,

                                onVolver = {
                                    pantalla = "productos"
                                },

                                onImagenSubida = {
                                    pantalla = "productos"
                                }
                            )
                        }

                        // ======================================
                        // EDITAR PRODUCTO
                        // ======================================

                    } else if (pantalla == "editar_producto") {

                        val producto = productoSeleccionado

                        if (producto != null) {

                            EditarProductoScreen(
                                token = token!!,
                                producto = producto,

                                onVolver = {
                                    pantalla = "productos"
                                },

                                onProductoActualizado = {
                                    productoSeleccionado = null
                                    pantalla = "productos"
                                }
                            )
                        }

                        // ======================================
                        // ADMINISTRAR FOTOGRAFÍAS (NUEVO)
                        // ======================================

                    } else if (pantalla == "administrar_imagenes") {

                        val producto = productoSeleccionado

                        if (producto != null) {

                            AdministrarImagenesScreen(
                                token = token!!,
                                producto = producto,

                                onVolver = {
                                    pantalla = "productos"
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
