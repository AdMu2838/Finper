package com.devgarden.finper.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.devgarden.finper.ui.theme.FinperTheme

/**
 * Composable reutilizable de BottomBar.
 * - items: lista de ImageVector que se mostrarán como iconos
 * - selectedIndex: índice del item seleccionado
 * - onItemSelected: callback cuando se selecciona un item
 */
@Composable
fun BottomBar(
    modifier: Modifier = Modifier,
    items: List<ImageVector>,
    selectedIndex: Int = 0,
    onItemSelected: (Int) -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, icon ->
                val isSelected = index == selectedIndex
                val backgroundColor = if (isSelected) Color(0xFFD9F3EC) else Color.Transparent
                val iconColor = if (isSelected) Color(0xFF00D1A1) else Color.Gray

                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(backgroundColor)
                        .clickable { onItemSelected(index) }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconColor)
                }
            }
        }
    }
}

// Un preview mínimo para ver la BottomBar en isolation si se desea
@Preview(showBackground = true)
@Composable
fun BottomBarPreview() {
    FinperTheme {
        BottomBar(
            items = listOf(
                Icons.Default.Home,
                Icons.Default.BarChart,
                Icons.Default.SwapHoriz,
                Icons.Default.AccountBalanceWallet,
                Icons.Default.Person
            ),
            selectedIndex = 0,
            onItemSelected = {}
        )
    }
}
