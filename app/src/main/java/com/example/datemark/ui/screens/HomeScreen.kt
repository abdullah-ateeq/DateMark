package com.example.datemark.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.datemark.domain.CalculatedDateMarkItem
import com.example.datemark.model.DateFilter
import com.example.datemark.model.DateMarkUiState
import com.example.datemark.model.DateSort
import com.example.datemark.model.DateStatus
import com.example.datemark.model.Recurrence
import com.example.datemark.ui.components.DateBadge
import com.example.datemark.ui.components.DateCard
import com.example.datemark.ui.components.HapticHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: DateMarkUiState,
    onSearchChange: (String) -> Unit,
    onFilterChange: (DateFilter) -> Unit,
    onSortChange: (DateSort) -> Unit,
    onDateClick: (String) -> Unit,
    onAddClick: () -> Unit
) {
    val context = LocalContext.current
    val hapticEnabled = uiState.settings.hapticFeedback
    var sortMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    HapticHelper.performHaptic(context, hapticEnabled)
                    onAddClick()
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.semantics { contentDescription = "Add important date." }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add Date",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 88.dp)
        ) {
            // Header Section
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "DateMark",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Keep track of important dates.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Prominent "Next Date" Card (if exists)
            val nextItem = uiState.summary.nextItem
            if (nextItem != null && uiState.searchQuery.isEmpty() && uiState.filter == DateFilter.ALL) {
                item {
                    NextDateHighlightCard(
                        item = nextItem,
                        onClick = {
                            HapticHelper.performHaptic(context, hapticEnabled)
                            onDateClick(nextItem.dateMark.id)
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Date Count Summary
            if (uiState.dates.isNotEmpty()) {
                item {
                    DateCountSummaryBanner(
                        total = uiState.summary.totalCount,
                        upcoming = uiState.summary.upcomingCount,
                        today = uiState.summary.todayCount,
                        passed = uiState.summary.passedCount
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Search important dates by label or note." },
                    placeholder = { Text("Search by label or note...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search query"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Filters & Sort Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LazyRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(DateFilter.values()) { filter ->
                            val selected = uiState.filter == filter
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    HapticHelper.performHaptic(context, hapticEnabled)
                                    onFilterChange(filter)
                                },
                                label = {
                                    Text(
                                        text = when (filter) {
                                            DateFilter.ALL -> "All"
                                            DateFilter.UPCOMING -> "Upcoming"
                                            DateFilter.TODAY -> "Today"
                                            DateFilter.PASSED -> "Passed"
                                            DateFilter.YEARLY -> "Yearly"
                                        }
                                    )
                                }
                            )
                        }
                    }

                    Box {
                        IconButton(
                            onClick = {
                                HapticHelper.performHaptic(context, hapticEnabled)
                                sortMenuExpanded = true
                            },
                            modifier = Modifier.semantics { contentDescription = "Sort dates" }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = "Sort options",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false }
                        ) {
                            DateSort.values().forEach { sortOption ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = when (sortOption) {
                                                DateSort.NEAREST -> "Nearest date"
                                                DateSort.FURTHEST -> "Furthest date"
                                                DateSort.NAME_AZ -> "Name A–Z"
                                                DateSort.NAME_ZA -> "Name Z–A"
                                                DateSort.RECENTLY_ADDED -> "Recently added"
                                            },
                                            fontWeight = if (uiState.sort == sortOption) FontWeight.Bold else FontWeight.Normal,
                                            color = if (uiState.sort == sortOption) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        HapticHelper.performHaptic(context, hapticEnabled)
                                        sortMenuExpanded = false
                                        onSortChange(sortOption)
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Today Section (if any dates occurring today)
            val todayItems = uiState.displayItems.filter { it.result.status == DateStatus.TODAY }
            if (todayItems.isNotEmpty() && uiState.filter != DateFilter.PASSED && uiState.filter != DateFilter.UPCOMING) {
                item {
                    Text(
                        text = "TODAY",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.tertiary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(todayItems, key = { "today_${it.dateMark.id}" }) { item ->
                    DateCard(
                        item = item,
                        onClick = {
                            HapticHelper.performHaptic(context, hapticEnabled)
                            onDateClick(item.dateMark.id)
                        },
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // Upcoming / Main Items
            val otherItems = if (todayItems.isNotEmpty() && uiState.filter != DateFilter.PASSED && uiState.filter != DateFilter.UPCOMING) {
                uiState.displayItems.filter { it.result.status != DateStatus.TODAY }
            } else {
                uiState.displayItems
            }

            if (otherItems.isNotEmpty()) {
                item {
                    Text(
                        text = when (uiState.filter) {
                            DateFilter.ALL -> "SAVED DATES"
                            DateFilter.UPCOMING -> "UPCOMING DATES"
                            DateFilter.TODAY -> "TODAY"
                            DateFilter.PASSED -> "PASSED DATES"
                            DateFilter.YEARLY -> "YEARLY RECURRING DATES"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(otherItems, key = { it.dateMark.id }) { item ->
                    DateCard(
                        item = item,
                        onClick = {
                            HapticHelper.performHaptic(context, hapticEnabled)
                            onDateClick(item.dateMark.id)
                        },
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }
            } else if (uiState.displayItems.isEmpty()) {
                item {
                    EmptyStateView(
                        hasDates = uiState.dates.isNotEmpty(),
                        isSearch = uiState.searchQuery.isNotEmpty(),
                        filter = uiState.filter,
                        onAddClick = {
                            HapticHelper.performHaptic(context, hapticEnabled)
                            onAddClick()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun NextDateHighlightCard(
    item: CalculatedDateMarkItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .semantics { contentDescription = "Next important date: ${item.dateMark.label}, ${item.statusText}" },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "NEXT DATE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Large Date Badge
            DateBadge(
                monthText = item.dayBadgeMonth,
                dayText = item.dayBadgeDay,
                status = item.result.status,
                isLarge = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = item.dateMark.label,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = when (item.result.status) {
                    DateStatus.TODAY -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.primary
                }
            ) {
                Text(
                    text = item.statusText,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = when (item.result.status) {
                        DateStatus.TODAY -> MaterialTheme.colorScheme.onTertiary
                        else -> MaterialTheme.colorScheme.onPrimary
                    }
                )
            }

            if (item.dateMark.recurrence == Recurrence.YEARLY) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Every year",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun DateCountSummaryBanner(
    total: Int,
    upcoming: Int,
    today: Int,
    passed: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$total ${if (total == 1) "date" else "important dates"}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "$upcoming upcoming • $today today • $passed passed",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyStateView(
    hasDates: Boolean,
    isSearch: Boolean,
    filter: DateFilter,
    onAddClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.DateRange,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (!hasDates) {
            Text(
                text = "No important dates yet.",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Add birthdays, deadlines, anniversaries, renewals, and other dates you want to remember.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(onClick = onAddClick) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ADD IMPORTANT DATE")
            }
        } else if (isSearch) {
            Text(
                text = "No matching dates.",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Try a different label or note.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        } else {
            val emptyFilterText = when (filter) {
                DateFilter.UPCOMING -> "No upcoming dates."
                DateFilter.TODAY -> "No dates today."
                DateFilter.PASSED -> "No passed dates."
                DateFilter.YEARLY -> "No yearly recurring dates."
                else -> "No dates found."
            }

            Text(
                text = emptyFilterText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}
