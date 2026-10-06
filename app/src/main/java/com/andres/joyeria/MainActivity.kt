package com.andres.joyeria
import com.andres.joyeria.ui.screens.ProductosScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.andres.joyeria.ui.screens.HomeScreen
import com.andres.joyeria.ui.screens.LoginScreen
import com.andres.joyeria.ui.theme.JoyeriaAppTheme
import com.andres.joyeria.ui.screens.AgregarProductoScreen
import com.andres.joyeria.data.api.Producto
import com.andres.joyeria.ui.screens.AgregarFotoScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            JoyeriaAppTheme {

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
                if (token == null) {

                    LoginScreen(
                        onLoginSuccess = { nuevoToken, nuevoNombre ->
                            token = nuevoToken
                            nombre = nuevoNombre
                            pantalla = "home"
                        }
                    )

                } else {

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
                                pantalla = "home"
                            }
                        )

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
                            }
                        )
                    }else if (pantalla == "agregar_producto") {

                        AgregarProductoScreen(
                            token = token!!,

                            onVolver = {
                                pantalla = "productos"
                            },

                            onProductoCreado = {
                                pantalla = "productos"
                            }
                        )
                    }else if (pantalla == "agregar_foto") {

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
                    }
                }
            }
        }
    }
}