package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Indigo600
import com.example.ui.theme.TagAmber
import com.example.ui.theme.TagPurple
import com.example.ui.theme.TagRed
import com.example.ui.viewmodel.StorageStats

@Composable
fun StorageUsageCard(
    stats: StorageStats,
    modifier: Modifier = Modifier
) {
    val totalGb = stats.totalBytes / (1024 * 1024 * 1024)
    val usedGb = String.format("%.1f", stats.usedBytes.toDouble() / (1024 * 1024 * 1024))
    val percentUsed = ((stats.usedBytes.toDouble() / stats.totalBytes) * 100).coerceAtLeast(12.0).toFloat()

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "STORAGE USAGE",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.80f),
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$usedGb GB",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = " / $totalGb GB",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.70f),
                        modifier = Modifier.padding(bottom = 2.dp, start = 4.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.70f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${percentUsed.toInt()}% USED",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Multi-color Progress Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))
            ) {
                val totalUsed = stats.usedBytes.coerceAtLeast(1).toDouble()
                val photoWeight = (stats.photosBytes / totalUsed).toFloat().coerceAtLeast(0.15f)
                val docWeight = (stats.docsBytes / totalUsed).toFloat().coerceAtLeast(0.15f)
                val systemWeight = (stats.systemBytes / totalUsed).toFloat().coerceAtLeast(0.15f)
                val vaultWeight = (stats.encryptedBytes / totalUsed).toFloat().coerceAtLeast(0.15f)

                Box(
                    modifier = Modifier
                        .weight(photoWeight)
                        .height(12.dp)
                        .background(Cyan500)
                )
                Box(
                    modifier = Modifier
                        .weight(docWeight)
                        .height(12.dp)
                        .background(Indigo600)
                )
                Box(
                    modifier = Modifier
                        .weight(systemWeight)
                        .height(12.dp)
                        .background(TagAmber)
                )
                Box(
                    modifier = Modifier
                        .weight(vaultWeight)
                        .height(12.dp)
                        .background(TagRed)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Legend
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                StorageLegendItem(label = "Media", color = Cyan500)
                StorageLegendItem(label = "Docs", color = Indigo600)
                StorageLegendItem(label = "System", color = TagAmber)
                StorageLegendItem(label = "Vault", color = TagRed)
            }
        }
    }
}

@Composable
private fun StorageLegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
        )
    }
}
