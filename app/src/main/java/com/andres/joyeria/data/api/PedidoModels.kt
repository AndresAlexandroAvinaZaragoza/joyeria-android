
package com.andres.joyeria.data.api

data class PedidosResponse(
    val message: String,
    val pedidos: List<Pedido>
)

data class PedidoResponse(
    val message: String,
    val pedido: Pedido
)

data class Pedido(
    val id_pedidos: Int,
    val numero_pedido: String,
    val subtotal: String,
    val costo_envio: String,
    val total: String,
    val estado: String,
    val user_id: Int,
    val created_at: String?,
    val usuario: PedidoUsuario?,
    val detalles: List<PedidoDetalle>?,
    val pago: PedidoPago?,
    val envio: PedidoEnvio?
)

data class PedidoUsuario(
    val id: Int?,
    val name: String?,
    val nombre: String?,
    val email: String?
) {
    fun nombreVisible(): String {
        return name ?: nombre ?: "Cliente sin nombre"
    }
}

data class PedidoDetalle(
    val id_pedido_detalle: Int,
    val precio_unitario: String,
    val cantidad: Int,
    val importe: String,
    val producto_id: Int,
    val producto: PedidoProducto?
)

data class PedidoProducto(
    val id_productos: Int,
    val nombre: String?,
    val sku: String?
)

data class PedidoPago(
    val estado: String?,
    val metodo_pago: String?,
    val monto: String?
)

data class PedidoEnvio(
    val estado: String?,
    val paqueteria: String?,
    val numero_guia: String?
)
