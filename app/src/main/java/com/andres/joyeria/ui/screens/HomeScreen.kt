package com.andres.joyeria.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andres.joyeria.ui.theme.*

@Composable
fun HomeScreen(
    nombre: String,
    onProductosClick: () -> Unit,
    onFotografiasClick: () -> Unit,
    onPedidosClick: () -> Unit,
    onCerrarSesion: () -> Unit,
    onEnviosClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JoyeriaFondo)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Joyería",
                    style = MaterialTheme.typography.headlineMedium,
                    color = JoyeriaOscuro,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.Default.Diamond,
                    contentDescription = "Logo Joyería",
                    tint = JoyeriaDorado,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Greeting Section
            Text(
                text = "ADMINISTRACIÓN",
                style = MaterialTheme.typography.labelMedium,
                color = JoyeriaDorado,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Hola, $nombre",
                style = MaterialTheme.typography.headlineLarge,
                color = JoyeriaOscuro,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "¿Qué quieres administrar hoy?",
                style = MaterialTheme.typography.bodyMedium,
                color = JoyeriaGris
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Menu Cards
            MenuCard(
                title = "Productos",
                subtitle = "Catálogo, precios e inventario",
                icon = Icons.Default.Diamond,
                isDark = true,
                onClick = onProductosClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            MenuCard(
                title = "Fotografías",
                subtitle = "Imágenes de tu colección",
                icon = Icons.Default.PhotoLibrary,
                isDark = false,
                onClick = onFotografiasClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            MenuCard(
                title = "Pedidos",
                subtitle = "Administración de pedidos",
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                isDark = false,
                onClick = onPedidosClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            MenuCard(
                title = "Envíos",
                subtitle = "Gestión de entregas",
                icon = Icons.Default.LocalShipping,
                isDark = false,
                onClick = onEnviosClick
            )

            Spacer(modifier = Modifier.weight(1f))

            // Cerrar sesión
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onCerrarSesion)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Cerrar sesión",
                    tint = JoyeriaOscuro,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cerrar sesión",
                    style = MaterialTheme.typography.bodyLarge,
                    color = JoyeriaOscuro,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun MenuCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isDark) JoyeriaOscuro else JoyeriaBlancoCampo
    val contentColor = if (isDark) JoyeriaBlancoCampo else JoyeriaOscuro
    val subtitleColor = if (isDark) JoyeriaBlancoCampo.copy(alpha = 0.7f) else JoyeriaGris
    val borderColor = if (isDark) Color.Transparent else JoyeriaBorde

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = if (isDark) null else BorderStroke(1.dp, borderColor),
        shadowElevation = if (isDark) 4.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isDark) JoyeriaDorado else JoyeriaOscuro,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = contentColor,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = subtitleColor
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.NorthEast,
                contentDescription = "Ir",
                tint = if (isDark) JoyeriaBlancoCampo else JoyeriaOscuro,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
