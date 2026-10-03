package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FuturisticCard(
    modifier: Modifier = Modifier,
    borderColor: Color = Color(0xFF8B5CF6).copy(alpha = 0.35f),
    cornerRadius: Dp = 20.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val bgBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF131A33).copy(alpha = 0.95f),
            Color(0xFF0C1124).copy(alpha = 0.98f)
        )
    )

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier
                .clip(shape)
                .border(BorderStroke(1.dp, borderColor), shape),
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .background(bgBrush)
                    .padding(16.dp)
            ) {
                content()
            }
        }
    } else {
        Box(
            modifier = modifier
                .clip(shape)
                .border(BorderStroke(1.dp, borderColor), shape)
                .background(bgBrush)
                .padding(16.dp)
        ) {
            content()
        }
    }
}

@Composable
fun StatusPill(
    text: String,
    color: Color = Color(0xFF10B981),
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.15f))
            .border(BorderStroke(1.dp, color.copy(alpha = 0.4f)), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = color,
            fontSize = 12.sp,
            style = MaterialTheme.typography.labelMedium
        )
    }
}
