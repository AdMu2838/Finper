package com.devgarden.finper.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devgarden.finper.ui.theme.PrimaryGreen

@Composable
fun TopRoundedHeader(
    title: String,
    modifier: Modifier = Modifier,
    showBack: Boolean = false,
    onBack: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
            .background(PrimaryGreen)
            .padding(start = 16.dp, end = 16.dp, top = 40.dp, bottom = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = title, color = Color.White, fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SummaryCard(
    modifier: Modifier = Modifier,
    balanceLabel: String,
    balanceValue: String,
    expenseLabel: String,
    expenseValue: String,
    progress: Float,
    progressLabel: String
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5FFF9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = balanceLabel, color = Color(0xFF15323B), fontSize = 12.sp)
                    Text(text = balanceValue, color = Color(0xFF15323B), fontSize = 20.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = expenseLabel, color = Color(0xFF15323B), fontSize = 12.sp)
                    Text(text = expenseValue, color = Color(0xFF00A78E), fontSize = 20.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(progress = { progress }, modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(8.dp)), color = Color(0xFF00B974), trackColor = Color(0xFFF0F6F3))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = progressLabel, fontSize = 12.sp, color = Color(0xFF15323B))
        }
    }
}

