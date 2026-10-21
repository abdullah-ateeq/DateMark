package com.example.datemark.ui.screens

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.datemark.domain.DateMarkFormatter
import com.example.datemark.model.AppDateFormat
import com.example.datemark.model.AppSettings
import com.example.datemark.model.AppTheme
import com.example.datemark.model.DateSort
import com.example.datemark.ui.components.ConfirmDeleteDialog
import com.example.datemark.ui.components.HapticHelper
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    today: LocalDate,
    onThemeChange: (AppTheme) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onHapticChange: (Boolean) -> Unit,
    onAnimationsChange: (Boolean) -> Unit,
    onConfirmDeleteChange: (Boolean) -> Unit,
    onDateFormatChange: (AppDateFormat) -> Unit,
    onDefaultSortChange: (DateSort) -> Unit,
    onResetSettings: () -> Unit,
    onClearAllDates: () -> Unit
) {
    val context = LocalContext.current
    val hapticEnabled = settings.hapticFeedback

    var showClearAllDialog by remember { mutableStateOf(false) }
    var dateFormatMenuExpanded by remember { mutableStateOf(false) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    if (showClearAllDialog) {
        ConfirmDeleteDialog(
            title = "Delete all saved dates?",
            message = "This cannot be undone. All saved date markers will be permanently removed.",
            confirmButtonText = "Delete All",
            onConfirm = {
                showClearAllDialog = false
                HapticHelper.performHaptic(context, hapticEnabled)
                onClearAllDates()
                Toast.makeText(context, "All dates deleted", Toast.LENGTH_SHORT).show()
            },
            onDismiss = {
                showClearAllDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") }
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
            // Appearance Section
            SettingsSectionHeader("APPEARANCE")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Theme",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        AppTheme.values().forEachIndexed { index, themeOption ->
                            SegmentedButton(
                                selected = settings.theme == themeOption,
                                onClick = {
                                    HapticHelper.performHaptic(context, hapticEnabled)
                                    onThemeChange(themeOption)
                                },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = AppTheme.values().size)
                            ) {
                                Text(themeOption.title)
                            }
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Spacer(modifier = Modifier.height(16.dp))
                        SettingsSwitchRow(
                            title = "Dynamic Color",
                            subtitle = "Use colors sampled from your system wallpaper",
                            checked = settings.dynamicColor,
                            onCheckedChange = {
                                HapticHelper.performHaptic(context, hapticEnabled)
                                onDynamicColorChange(it)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Behavior Section
            SettingsSectionHeader("BEHAVIOR")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingsSwitchRow(
                        title = "Haptic Feedback",
                        subtitle = "Tactile vibration feedback on interactions",
                        checked = settings.hapticFeedback,
                        onCheckedChange = {
                            HapticHelper.performHaptic(context, true)
                            onHapticChange(it)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingsSwitchRow(
                        title = "Animations",
                        subtitle = "Enable smooth UI transitions",
                        checked = settings.animations,
                        onCheckedChange = {
                            HapticHelper.performHaptic(context, hapticEnabled)
                            onAnimationsChange(it)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingsSwitchRow(
                        title = "Confirm Delete",
                        subtitle = "Ask for confirmation before deleting dates",
                        checked = settings.confirmDelete,
                        onCheckedChange = {
                            HapticHelper.performHaptic(context, hapticEnabled)
                            onConfirmDeleteChange(it)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Date Display Section
            SettingsSectionHeader("DATE DISPLAY")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Date Format",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Preview: ${DateMarkFormatter.formatDate(today, settings.dateFormat)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        AppDateFormat.values().forEachIndexed { index, formatOption ->
                            SegmentedButton(
                                selected = settings.dateFormat == formatOption,
                                onClick = {
                                    HapticHelper.performHaptic(context, hapticEnabled)
                                    onDateFormatChange(formatOption)
                                },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = AppDateFormat.values().size)
                            ) {
                                Text(
                                    text = when (formatOption) {
                                        AppDateFormat.SYSTEM_DEFAULT -> "System"
                                        AppDateFormat.SHORT -> "Short"
                                        AppDateFormat.MEDIUM -> "Med"
                                        AppDateFormat.LONG -> "Long"
                                    },
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Home Default Sort Section
            SettingsSectionHeader("HOME")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Default Sort",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Initial sorting order for saved dates",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        DateSort.values().forEach { sortOption ->
                            val isSelected = settings.defaultSort == sortOption
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        HapticHelper.performHaptic(context, hapticEnabled)
                                        onDefaultSortChange(sortOption)
                                    }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = when (sortOption) {
                                        DateSort.NEAREST -> "Nearest date"
                                        DateSort.FURTHEST -> "Furthest date"
                                        DateSort.NAME_AZ -> "Name A–Z"
                                        DateSort.NAME_ZA -> "Name Z–A"
                                        DateSort.RECENTLY_ADDED -> "Recently added"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                if (isSelected) {
                                    Text(
                                        text = "✓",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Data Management Section
            SettingsSectionHeader("DATA")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedButton(
                        onClick = {
                            HapticHelper.performHaptic(context, hapticEnabled)
                            onResetSettings()
                            Toast.makeText(context, "Settings reset to defaults", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("RESET SETTINGS")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            HapticHelper.performHaptic(context, hapticEnabled)
                            showClearAllDialog = true
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("CLEAR ALL DATES")
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
    )
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
