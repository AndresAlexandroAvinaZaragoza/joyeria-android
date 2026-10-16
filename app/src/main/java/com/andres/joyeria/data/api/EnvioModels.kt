package com.andres.joyeria.data.api

data class EnviosResponse(
    val message: String,
    val envios: List<Envio>
)

data class EnvioResponse(
    val message: String,
    val envio: Envio
)

data class Envio(
    val id_envios: Int,
    val direccion_destino: String,
    val paqueteria: String?,
    val numero_guia: String?,
    val estado: String,
    val fecha_envio: String?,
    val fecha_entrega: String?,
    val pedido_id: Int,
    val pedido: EnvioPedido?
)

data class EnvioPedido(
    val id_pedidos: Int,
    val numero_pedido: String,
    val usuario: PedidoUsuario?
)

data class ActualizarEnvioRequest(
    val estado: String,
    val paqueteria: String?,
    val numero_guia: String?
)

