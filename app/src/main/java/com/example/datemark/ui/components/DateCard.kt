package com.example.datemark.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.datemark.domain.CalculatedDateMarkItem
import com.example.datemark.model.DateStatus
import com.example.datemark.model.Recurrence

@Composable
fun DateCard(
    item: CalculatedDateMarkItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateMark = item.dateMark
    val result = item.result

    val statusColor = when (result.status) {
        DateStatus.TODAY -> MaterialTheme.colorScheme.tertiary
        DateStatus.UPCOMING -> MaterialTheme.colorScheme.primary
        DateStatus.PASSED -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    }

    val accessibilityText = buildString {
        append("Important date: ${dateMark.label}. ")
        append("Date: ${item.formattedDate}. ")
        append(
            when (result.status) {
                DateStatus.TODAY -> "Date is today. "
                DateStatus.UPCOMING -> "${result.dayDifference} days remaining. "
                DateStatus.PASSED -> "${kotlin.math.abs(result.dayDifference)} days ago. "
            }
        )
        if (dateMark.recurrence == Recurrence.YEARLY) {
            append("Repeats every year. ")
        }
        if (dateMark.note.isNotBlank()) {
            append("Note: ${dateMark.note}. ")
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .semantics { contentDescription = accessibilityText },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (result.status == DateStatus.TODAY) {
                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Calendar Badge
            DateBadge(
                monthText = item.dayBadgeMonth,
                dayText = item.dayBadgeDay,
                status = result.status
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Label & Date Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = dateMark.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = item.formattedDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (dateMark.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = dateMark.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (dateMark.recurrence == Recurrence.YEARLY) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = "Repeats every year",
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Every year",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Status Badge / Days Left Pill
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = when (result.status) {
                    DateStatus.TODAY -> MaterialTheme.colorScheme.tertiary
                    DateStatus.UPCOMING -> MaterialTheme.colorScheme.primaryContainer
                    DateStatus.PASSED -> MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Text(
                    text = item.statusText,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = when (result.status) {
                        DateStatus.TODAY -> MaterialTheme.colorScheme.onTertiary
                        DateStatus.UPCOMING -> MaterialTheme.colorScheme.onPrimaryContainer
                        DateStatus.PASSED -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}
