package com.andres.joyeria.data.api
import com.andres.joyeria.ui.screens.ActualizarProductoRequest
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Body
import retrofit2.http.POST
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.Response
import retrofit2.http.PUT
import retrofit2.http.DELETE

data class LoginRequest(
    val email: String,
    val password: String
)

data class User(
    val id: Int,
    val name: String,
    val apellidos: String?,
    val email: String,
    val rol_id: Int
)

data class LoginResponse(
    val message: String,
    val user: User,
    val token: String
)

data class Categoria(
    val id_categorias: Int,
    val nombre: String
)

data class Inventario(
    val id_inventario: Int,
    val stock_actual: Int,
    val stock_minimo: Int,
    val producto_id: Int
)

data class ProductoImagen(
    val id_img: Int,
    val url_img: String,
    val es_principal: Boolean,
    val orden: Int,
    val producto_id: Int
)

data class Producto(
    val id_productos: Int,
    val sku: String,
    val nombre: String,
    val descripcion: String?,
    val material: String,
    val peso_gr: String?,
    val talla_medida: String?,
    val precio_costo: String,
    val precio_venta: String,
    val status: Boolean,
    val id_categoria: Int,
    val categoria: Categoria?,
    val inventario: Inventario?,
    val imagenes: List<ProductoImagen>
)

data class ProductosResponse(
    val message: String,
    val productos: List<Producto>
)

data class CrearProductoRequest(
    val sku: String,
    val nombre: String,
    val descripcion: String?,
    val id_categoria: Int,
    val material: String,
    val peso_gr: Double?,
    val talla_medida: String?,
    val precio_costo: Double,
    val precio_venta: Double,
    val stock_actual: Int,
    val stock_minimo: Int
)

data class CrearProductoResponse(
    val message: String,
    val producto: Producto
)

data class SubirImagenResponse(
    val message: String,
    val producto: Producto
)
interface ApiService {
    //Login
    @POST("api/login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse

    // Obtener productos
    @GET("api/productos")
    suspend fun obtenerProductos(
        @Header("Authorization") authorization: String
    ): ProductosResponse

    @POST("api/productos")
    suspend fun crearProducto(
        @Header("Authorization") authorization: String,
        @Body producto: CrearProductoRequest
    ): CrearProductoResponse

    @Multipart
    @POST("api/productos/{producto}/imagenes")
    suspend fun subirImagenProducto(
        @Header("Authorization") authorization: String,
        @Path("producto") productoId: Int,
        @Part imagen: MultipartBody.Part,
        @Part("es_principal") esPrincipal: RequestBody
    ): SubirImagenResponse

    @PUT("api/productos/{producto}")
    suspend fun actualizarProducto(
        @Header("Authorization") authorization: String,
        @Path("producto") productoId: Int,
        @Body datos: ActualizarProductoRequest
    ): Response<Producto>

    @DELETE("api/productos/{producto}")
    suspend fun eliminarProducto(
        @Header("Authorization") authorization: String,
        @Path("producto") productoId: Int
    ): Response<Unit>

    @Multipart
    @POST("api/productos/{producto}/imagenes/{imagen}")
    suspend fun actualizarImagenProducto(
        @Header("Authorization") authorization: String,
        @Path("producto") productoId: Int,
        @Path("imagen") imagenId: Int,
        @Part imagen: MultipartBody.Part?,
        @Part("es_principal") esPrincipal: RequestBody,
        @Part("orden") orden: RequestBody
    ): Response<Unit>

    @DELETE("api/productos/{producto}/imagenes/{imagen}")
    suspend fun eliminarImagenProducto(
        @Header("Authorization") authorization: String,
        @Path("producto") productoId: Int,
        @Path("imagen") imagenId: Int
    ): Response<Unit>
}