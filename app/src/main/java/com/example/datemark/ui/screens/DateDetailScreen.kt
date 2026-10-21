package com.example.datemark.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.datemark.domain.CalculatedDateMarkItem
import com.example.datemark.model.DateStatus
import com.example.datemark.model.Recurrence
import com.example.datemark.ui.components.ConfirmDeleteDialog
import com.example.datemark.ui.components.DateBadge
import com.example.datemark.ui.components.HapticHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateDetailScreen(
    item: CalculatedDateMarkItem?,
    confirmDeleteSetting: Boolean,
    hapticEnabled: Boolean,
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit,
    onDeleteConfirm: (String) -> Unit,
    getShareText: (String) -> String?
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (item == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Date Details") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Date not found.")
            }
        }
        return
    }

    val dateMark = item.dateMark
    val result = item.result

    if (showDeleteDialog) {
        ConfirmDeleteDialog(
            title = "Delete this date?",
            message = "Are you sure you want to delete \"${dateMark.label}\"? This action cannot be undone.",
            confirmButtonText = "Delete",
            onConfirm = {
                showDeleteDialog = false
                HapticHelper.performHaptic(context, hapticEnabled)
                onDeleteConfirm(dateMark.id)
            },
            onDismiss = {
                showDeleteDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = dateMark.label,
                        maxLines = 1,
                        modifier = Modifier.semantics { contentDescription = "Important date label." }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            HapticHelper.performHaptic(context, hapticEnabled)
                            onEditClick(dateMark.id)
                        },
                        modifier = Modifier.semantics { contentDescription = "Edit important date." }
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(
                        onClick = {
                            HapticHelper.performHaptic(context, hapticEnabled)
                            if (confirmDeleteSetting) {
                                showDeleteDialog = true
                            } else {
                                onDeleteConfirm(dateMark.id)
                            }
                        },
                        modifier = Modifier.semantics { contentDescription = "Delete important date." }
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Main Feature Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (result.status == DateStatus.TODAY) {
                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    }
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    DateBadge(
                        monthText = item.dayBadgeMonth,
                        dayText = item.dayBadgeDay,
                        status = result.status,
                        isLarge = true
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = dateMark.label,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.semantics { contentDescription = "Important date label." }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = item.formattedDate,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.semantics { contentDescription = "Important date." }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Primary Status Banner
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = when (result.status) {
                            DateStatus.TODAY -> MaterialTheme.colorScheme.tertiary
                            DateStatus.UPCOMING -> MaterialTheme.colorScheme.primary
                            DateStatus.PASSED -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ) {
                        Text(
                            text = item.statusText,
                            modifier = Modifier
                                .padding(horizontal = 18.dp, vertical = 8.dp)
                                .semantics {
                                    contentDescription = when (result.status) {
                                        DateStatus.TODAY -> "Date is today."
                                        DateStatus.UPCOMING -> "${result.dayDifference} days remaining."
                                        DateStatus.PASSED -> "${kotlin.math.abs(result.dayDifference)} days ago."
                                    }
                                },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = when (result.status) {
                                DateStatus.TODAY -> MaterialTheme.colorScheme.onTertiary
                                DateStatus.UPCOMING -> MaterialTheme.colorScheme.onPrimary
                                DateStatus.PASSED -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // REPEAT SECTION
            DetailSectionTitle("REPEAT")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = item.recurrenceText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // NOTE SECTION
            DetailSectionTitle("NOTE")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Text(
                    text = if (dateMark.note.isNotBlank()) dateMark.note else "No note added.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (dateMark.note.isNotBlank()) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    },
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons: COPY, SHARE, EDIT, DELETE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalButton(
                    onClick = {
                        HapticHelper.performHaptic(context, hapticEnabled)
                        val text = getShareText(dateMark.id) ?: ""
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("DateMark", text)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .semantics { contentDescription = "Copy date information." }
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("COPY")
                }

                FilledTonalButton(
                    onClick = {
                        HapticHelper.performHaptic(context, hapticEnabled)
                        val text = getShareText(dateMark.id) ?: ""
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, dateMark.label)
                            putExtra(Intent.EXTRA_TEXT, text)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share DateMark"))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .semantics { contentDescription = "Share date information." }
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SHARE")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        HapticHelper.performHaptic(context, hapticEnabled)
                        onEditClick(dateMark.id)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .semantics { contentDescription = "Edit important date." }
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("EDIT")
                }

                OutlinedButton(
                    onClick = {
                        HapticHelper.performHaptic(context, hapticEnabled)
                        if (confirmDeleteSetting) {
                            showDeleteDialog = true
                        } else {
                            onDeleteConfirm(dateMark.id)
                        }
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier
                        .weight(1f)
                        .semantics { contentDescription = "Delete important date." }
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("DELETE")
                }
            }
        }
    }
}

@Composable
private fun DetailSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
    )
}
