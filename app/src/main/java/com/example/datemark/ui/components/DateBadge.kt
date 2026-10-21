package com.example.datemark.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.datemark.model.DateStatus

@Composable
fun DateBadge(
    monthText: String,
    dayText: String,
    status: DateStatus,
    modifier: Modifier = Modifier,
    isLarge: Boolean = false
) {
    val headerColor = when (status) {
        DateStatus.TODAY -> MaterialTheme.colorScheme.tertiary
        DateStatus.UPCOMING -> MaterialTheme.colorScheme.primary
        DateStatus.PASSED -> MaterialTheme.colorScheme.outline
    }

    val headerTextColor = when (status) {
        DateStatus.TODAY -> MaterialTheme.colorScheme.onTertiary
        DateStatus.UPCOMING -> MaterialTheme.colorScheme.onPrimary
        DateStatus.PASSED -> MaterialTheme.colorScheme.surfaceVariant
    }

    val widthDp = if (isLarge) 84.dp else 56.dp
    val headerPaddingVertical = if (isLarge) 6.dp else 3.dp
    val dayFontSize = if (isLarge) 26.sp else 18.sp
    val monthFontSize = if (isLarge) 13.sp else 10.sp
    val shape = RoundedCornerShape(10.dp)

    Column(
        modifier = modifier
            .width(widthDp)
            .clip(shape)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                shape = shape
            )
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Month top header strip
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerColor)
                .padding(vertical = headerPaddingVertical),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = monthText,
                color = headerTextColor,
                fontSize = monthFontSize,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )
        }

        // Day bottom number
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = if (isLarge) 8.dp else 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = dayText,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = dayFontSize,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
        }
    }
}
