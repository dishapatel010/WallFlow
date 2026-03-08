package com.ammar.wallflow.ui.screens.collections

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ammar.wallflow.R
import com.ammar.wallflow.model.CollectionCategory
import com.ammar.wallflow.model.Source
import com.ammar.wallflow.ui.screens.collections.AppearanceFilter
import com.ammar.wallflow.ui.screens.collections.PurityFilter
import com.ammar.wallflow.ui.theme.WallFlowTheme
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal fun LazyStaggeredGridScope.header(
    selectedCategory: CollectionCategory = CollectionCategory.FAVORITES,
    onCategoryClick: (CollectionCategory) -> Unit = {},
    selectedSourceFilter: Source? = null,
    onSourceFilterClick: (Source?) -> Unit = {},
    selectedPurityFilter: PurityFilter = PurityFilter.ALL,
    onPurityFilterClick: (PurityFilter) -> Unit = {},
) {
    item(span = StaggeredGridItemSpan.FullLine) {
        Column {
            CategoriesRow(
                selected = selectedCategory,
                onCategoryClick = onCategoryClick,
            )
            AllFiltersRow(
                selectedSource = selectedSourceFilter,
                onSourceClick = onSourceFilterClick,
                selectedPurityFilter = selectedPurityFilter,
                onPurityClick = onPurityFilterClick,
            )
        }
    }
}

@Composable
fun CategoriesRow(
    modifier: Modifier = Modifier,
    selected: CollectionCategory = CollectionCategory.FAVORITES,
    onCategoryClick: (CollectionCategory) -> Unit = {},
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CollectionCategory.entries.forEach {
            CategoryChip(
                category = it,
                selected = selected == it,
                onClick = { onCategoryClick(it) },
            )
        }
    }
}

/**
 * Two visually distinct groups in one scrollable row, separated by a vertical divider.
 * Group 1 — Source (All / Wallhaven / Reddit)
 * Group 2 — Content (All / SFW / NSFW), with colour-coded chips so they are never confused
 *           with the source chips.
 */
@Composable
fun AllFiltersRow(
    modifier: Modifier = Modifier,
    selectedSource: Source? = null,
    onSourceClick: (Source?) -> Unit = {},
    selectedPurityFilter: PurityFilter = PurityFilter.ALL,
    onPurityClick: (PurityFilter) -> Unit = {},
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // ── Source group label ──────────────────────────────────────────────
        Text(
            text = stringResource(R.string.source_label),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 2.dp),
        )
        val sources = listOf<Source?>(null, Source.WALLHAVEN, Source.REDDIT)
        sources.forEach { source ->
            SourceFilterChip(
                source = source,
                selected = selectedSource == source,
                onClick = { onSourceClick(source) },
            )
        }

        // ── Visual separator ────────────────────────────────────────────────
        VerticalDivider(
            modifier = Modifier
                .height(24.dp)
                .padding(horizontal = 2.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
        )

        // ── Purity group label ──────────────────────────────────────────────
        Text(
            text = stringResource(R.string.content_label),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 2.dp),
        )
        PurityFilter.entries.forEach { purityFilter ->
            PurityFilterChip(
                purityFilter = purityFilter,
                selected = selectedPurityFilter == purityFilter,
                onClick = { onPurityClick(purityFilter) },
            )
        }
    }
}

@Composable
fun SourceFiltersRow(
    modifier: Modifier = Modifier,
    selectedSource: Source? = null,
    onSourceClick: (Source?) -> Unit = {},
) {
    val sources = listOf<Source?>(null, Source.WALLHAVEN, Source.REDDIT)
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        sources.forEach { source ->
            SourceFilterChip(
                source = source,
                selected = selectedSource == source,
                onClick = { onSourceClick(source) },
            )
        }
    }
}

@Composable
private fun SourceFilterChip(
    source: Source?,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: () -> Unit = {},
) {
    FilterChip(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderWidth = 0.dp,
            borderColor = Color.Transparent,
        ),
        leadingIcon = if (selected) {
            {
                Icon(
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                    painter = painterResource(R.drawable.baseline_check_24),
                    contentDescription = null,
                )
            }
        } else {
            null
        },
        label = {
            Text(
                text = stringResource(
                    when (source) {
                        null -> R.string.all
                        Source.WALLHAVEN -> R.string.wallhaven
                        Source.REDDIT -> R.string.reddit
                        Source.LOCAL -> R.string.local
                    },
                ),
            )
        },
        selected = selected,
        onClick = onClick,
    )
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewHeader() {
    WallFlowTheme {
        Surface {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
            ) {
                header()
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewAllFiltersRow() {
    WallFlowTheme {
        Surface {
            Column(modifier = Modifier.padding(8.dp)) {
                AllFiltersRow(
                    selectedSource = null,
                    selectedPurityFilter = PurityFilter.SFW,
                )
            }
        }
    }
}

@Composable
private fun CategoryChip(
    category: CollectionCategory,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: () -> Unit = {},
) {
    FilterChip(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderWidth = 0.dp,
            borderColor = Color.Transparent,
        ),
        leadingIcon = {
            AnimatedContent(
                targetState = selected,
                label = "leading icon",
            ) {
                Icon(
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                    painter = painterResource(
                        if (it) {
                            R.drawable.baseline_check_24
                        } else {
                            when (category) {
                                CollectionCategory.FAVORITES -> R.drawable.baseline_favorite_24
                                CollectionCategory.LIGHT_DARK -> R.drawable.baseline_light_dark
                            }
                        },
                    ),
                    contentDescription = null,
                )
            }
        },
        label = {
            Text(
                text = stringResource(
                    when (category) {
                        CollectionCategory.FAVORITES -> R.string.favorites
                        CollectionCategory.LIGHT_DARK -> R.string.light_dark
                    },
                ),
            )
        },
        selected = selected,
        onClick = onClick,
    )
}

@Composable
private fun PurityFilterChip(
    purityFilter: PurityFilter,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: () -> Unit = {},
) {
    // Colour-coded so SFW/NSFW are visually unambiguous even when both "All" labels appear
    val selectedContainerColor = when (purityFilter) {
        PurityFilter.ALL -> MaterialTheme.colorScheme.primaryContainer
        PurityFilter.SFW -> MaterialTheme.colorScheme.tertiaryContainer
        PurityFilter.NSFW -> MaterialTheme.colorScheme.errorContainer
    }
    val selectedLabelColor = when (purityFilter) {
        PurityFilter.ALL -> MaterialTheme.colorScheme.onPrimaryContainer
        PurityFilter.SFW -> MaterialTheme.colorScheme.onTertiaryContainer
        PurityFilter.NSFW -> MaterialTheme.colorScheme.onErrorContainer
    }
    FilterChip(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = selectedContainerColor,
            selectedLabelColor = selectedLabelColor,
            selectedLeadingIconColor = selectedLabelColor,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderWidth = 0.dp,
            borderColor = Color.Transparent,
        ),
        leadingIcon = if (selected) {
            {
                Icon(
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                    painter = painterResource(R.drawable.baseline_check_24),
                    contentDescription = null,
                )
            }
        } else {
            null
        },
        label = {
            Text(
                text = stringResource(
                    when (purityFilter) {
                        PurityFilter.ALL -> R.string.all
                        PurityFilter.SFW -> R.string.sfw
                        PurityFilter.NSFW -> R.string.nsfw
                    },
                ),
            )
        },
        selected = selected,
        onClick = onClick,
    )
}
@Composable
private fun FilterSectionLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier,
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun AppearanceFilterChip(
    filter: AppearanceFilter,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: () -> Unit = {},
) {
    FilterChip(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderWidth = 0.dp,
            borderColor = Color.Transparent,
        ),
        leadingIcon = if (selected) {
            {
                Icon(
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                    painter = painterResource(R.drawable.baseline_check_24),
                    contentDescription = null,
                )
            }
        } else {
            null
        },
        label = {
            Text(
                text = stringResource(
                    when (filter) {
                        AppearanceFilter.LIGHT -> R.string.light
                        AppearanceFilter.DARK -> R.string.dark
                        AppearanceFilter.ALL -> R.string.all
                    },
                ),
            )
        },
        selected = selected,
        onClick = onClick,
    )
}

@Composable
internal fun FiltersChip(
    modifier: Modifier = Modifier,
    activeFilterCount: Int = 0,
    onClick: () -> Unit = {},
) {
    FilterChip(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onTertiaryContainer,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = activeFilterCount > 0,
            borderWidth = 0.dp,
            borderColor = Color.Transparent,
        ),
        leadingIcon = {
            Icon(
                modifier = Modifier.size(FilterChipDefaults.IconSize),
                painter = painterResource(R.drawable.baseline_filter_alt_24),
                contentDescription = null,
            )
        },
        label = {
            if (activeFilterCount > 0) {
                Text(stringResource(R.string.filters) + " \u00B7 $activeFilterCount")
            } else {
                Text(stringResource(R.string.filters))
            }
        },
        selected = activeFilterCount > 0,
        onClick = onClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun CollectionsFilterSheet(
    modifier: Modifier = Modifier,
    selectedSource: Source? = null,
    selectedPurityFilter: PurityFilter = PurityFilter.ALL,
    selectedAppearanceFilter: AppearanceFilter = AppearanceFilter.ALL,
    selectedDateFilter: LocalDate? = null,
    showDateSeparators: Boolean = false,
    availableDates: Set<LocalDate> = emptySet(),
    isFavoritesCategory: Boolean = true,
    onDismissRequest: () -> Unit = {},
    onApply: (
        source: Source?,
        purity: PurityFilter,
        appearance: AppearanceFilter,
        date: LocalDate?,
        showSeparators: Boolean,
    ) -> Unit = { _, _, _, _, _ -> },
) {
    var draftSource by remember(selectedSource) { mutableStateOf(selectedSource) }
    var draftPurity by remember(selectedPurityFilter) { mutableStateOf(selectedPurityFilter) }
    var draftAppearance by remember(selectedAppearanceFilter) { mutableStateOf(selectedAppearanceFilter) }
    var draftDate by remember(selectedDateFilter) { mutableStateOf(selectedDateFilter) }
    var draftShowSep by remember(showDateSeparators) { mutableStateOf(showDateSeparators) }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // ── Date picker dialog ───────────────────────────────────────────────
    if (showDatePickerDialog) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = draftDate?.let { d ->
                java.time.LocalDate.of(d.year, d.monthNumber, d.dayOfMonth)
                    .atStartOfDay(java.time.ZoneOffset.UTC)
                    .toInstant()
                    .toEpochMilli()
            },
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    if (availableDates.isEmpty()) return true
                    val date = Instant.fromEpochMilliseconds(utcTimeMillis)
                        .toLocalDateTime(TimeZone.UTC).date
                    return date in availableDates
                }
            },
        )
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        draftDate = pickerState.selectedDateMillis?.let { millis ->
                            Instant.fromEpochMilliseconds(millis)
                                .toLocalDateTime(TimeZone.UTC).date
                        }
                        showDatePickerDialog = false
                    },
                ) { Text(stringResource(android.R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }

    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.filters),
                style = MaterialTheme.typography.titleMedium,
            )
            HorizontalDivider()

            // ── Source ──────────────────────────────────────────────────────
            FilterSectionLabel(text = stringResource(R.string.source_label))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val sources = listOf<Source?>(null, Source.WALLHAVEN, Source.REDDIT)
                sources.forEach { s ->
                    SourceFilterChip(
                        source = s,
                        selected = draftSource == s,
                        onClick = { draftSource = s },
                    )
                }
            }

            // ── Content ─────────────────────────────────────────────────────
            FilterSectionLabel(text = stringResource(R.string.content_label))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PurityFilter.entries.forEach { p ->
                    PurityFilterChip(
                        purityFilter = p,
                        selected = draftPurity == p,
                        onClick = { draftPurity = p },
                    )
                }
            }

            // ── Appearance ──────────────────────────────────────────────────
            FilterSectionLabel(text = stringResource(R.string.appearance_label))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppearanceFilterChip(
                    filter = AppearanceFilter.LIGHT,
                    selected = draftAppearance == AppearanceFilter.LIGHT,
                    onClick = {
                        draftAppearance = if (draftAppearance == AppearanceFilter.LIGHT) {
                            AppearanceFilter.ALL
                        } else {
                            AppearanceFilter.LIGHT
                        }
                    },
                )
                AppearanceFilterChip(
                    filter = AppearanceFilter.DARK,
                    selected = draftAppearance == AppearanceFilter.DARK,
                    onClick = {
                        draftAppearance = if (draftAppearance == AppearanceFilter.DARK) {
                            AppearanceFilter.ALL
                        } else {
                            AppearanceFilter.DARK
                        }
                    },
                )
            }

            // ── Date filter + separator toggle (favorites only) ─────────────
            if (isFavoritesCategory) {
                HorizontalDivider()

                FilterSectionLabel(text = stringResource(R.string.date_label))

                val formattedDraftDate = remember(draftDate) {
                    draftDate?.let { d ->
                        java.time.LocalDate.of(d.year, d.monthNumber, d.dayOfMonth)
                            .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FilterChip(
                        selected = draftDate != null,
                        onClick = { showDatePickerDialog = true },
                        label = {
                            Text(formattedDraftDate ?: stringResource(R.string.any_date))
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize),
                            )
                        },
                    )
                    if (draftDate != null) {
                        TextButton(onClick = { draftDate = null }) {
                            Text(stringResource(R.string.clear))
                        }
                    }
                }

                HorizontalDivider()

                FilterSectionLabel(text = stringResource(R.string.display_options))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.show_date_separators),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Switch(
                        checked = draftShowSep,
                        onCheckedChange = { draftShowSep = it },
                    )
                }
            }

            HorizontalDivider()

            // ── Buttons ─────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        draftSource = null
                        draftPurity = PurityFilter.ALL
                        draftAppearance = AppearanceFilter.ALL
                        draftDate = null
                        draftShowSep = false
                    },
                ) { Text(stringResource(R.string.reset)) }
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onApply(draftSource, draftPurity, draftAppearance, draftDate, draftShowSep)
                        onDismissRequest()
                    },
                ) { Text(stringResource(R.string.apply)) }
            }
        }
    }
}