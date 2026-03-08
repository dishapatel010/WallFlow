package com.ammar.wallflow.ui.screens.collections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.ammar.wallflow.R
import com.ammar.wallflow.data.preferences.GridColType
import com.ammar.wallflow.data.preferences.LayoutPreferences
import com.ammar.wallflow.data.preferences.ViewedWallpapersLook
import com.ammar.wallflow.extensions.rememberLazyStaggeredGridState
import com.ammar.wallflow.extensions.toDp
import com.ammar.wallflow.extensions.Saver
import com.ammar.wallflow.model.CollectionCategory
import com.ammar.wallflow.model.Favorite
import com.ammar.wallflow.model.LightDark
import com.ammar.wallflow.model.LightDarkType
import com.ammar.wallflow.model.Purity
import com.ammar.wallflow.model.Source
import com.ammar.wallflow.model.Viewed
import com.ammar.wallflow.model.Wallpaper
import com.ammar.wallflow.model.reddit.RedditWallpaper
import com.ammar.wallflow.model.wallhaven.WallhavenTag
import com.ammar.wallflow.model.wallhaven.WallhavenUploader
import com.ammar.wallflow.ui.common.BottomBarAwareHorizontalTwoPane
import com.ammar.wallflow.ui.common.PlaceholderWallpaperCard
import com.ammar.wallflow.ui.common.WallpaperCard
import com.ammar.wallflow.ui.common.WallpaperQuickActionsSheet
import com.ammar.wallflow.ui.common.WallpaperStaggeredGrid
import com.ammar.wallflow.ui.common.getAdaptiveMinWidth
import com.ammar.wallflow.ui.screens.collections.AppearanceFilter
import com.ammar.wallflow.ui.screens.collections.CategoriesRow
import com.ammar.wallflow.ui.screens.collections.CollectionsFeedItem
import com.ammar.wallflow.ui.screens.collections.CollectionsFilterSheet
import com.ammar.wallflow.ui.screens.collections.FiltersChip
import com.ammar.wallflow.ui.screens.collections.PurityFilter
import com.ammar.wallflow.ui.wallpaperviewer.WallpaperViewer
import com.ammar.wallflow.utils.DownloadStatus
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun CollectionsScreenContent(
    feedItems: LazyPagingItems<CollectionsFeedItem>,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(8.dp),
    favorites: ImmutableList<Favorite> = persistentListOf(),
    viewedList: ImmutableList<Viewed> = persistentListOf(),
    viewedWallpapersLook: ViewedWallpapersLook = ViewedWallpapersLook.DIM_WITH_LABEL,
    lightDarkList: ImmutableList<LightDark> = persistentListOf(),
    blurSketchy: Boolean = false,
    blurNsfw: Boolean = false,
    selectedWallpaper: Wallpaper? = null,
    showSelection: Boolean = false,
    layoutPreferences: LayoutPreferences = LayoutPreferences(),
    selectedCategory: CollectionCategory = CollectionCategory.FAVORITES,
    selectedSourceFilter: Source? = null,
    selectedPurityFilter: PurityFilter = PurityFilter.ALL,
    selectedAppearanceFilter: AppearanceFilter = AppearanceFilter.ALL,
    selectedDateFilter: LocalDate? = null,
    showDateSeparators: Boolean = false,
    availableDates: ImmutableSet<LocalDate> = persistentSetOf(),
    onCategoryClick: (CollectionCategory) -> Unit = {},
    onApplyFilters: (
        source: Source?,
        purity: PurityFilter,
        appearance: AppearanceFilter,
        date: LocalDate?,
        showSeparators: Boolean,
    ) -> Unit = { _, _, _, _, _ -> },
    quickActionsWallpaper: Wallpaper? = null,
    onWallpaperLongClick: (Wallpaper) -> Unit = {},
    onQuickActionsDismiss: () -> Unit = {},
    onQuickActionsFavoriteClick: (Wallpaper) -> Unit = {},
    onQuickActionsApplyWallpaperClick: (Wallpaper) -> Unit = {},
    onQuickActionsDownloadClick: (Wallpaper) -> Unit = {},
    onQuickActionsShareClick: (Wallpaper) -> Unit = {},
    showTelegram: Boolean = false,
    onQuickActionsTelegramClick: (Wallpaper) -> Unit = {},
    fullWallpaper: Wallpaper? = null,
    fullWallpaperActionsVisible: Boolean = true,
    fullWallpaperDownloadStatus: DownloadStatus? = null,
    fullWallpaperLoading: Boolean = false,
    showFullWallpaperInfo: Boolean = false,
    isFullWallpaperFavorite: Boolean = false,
    onWallpaperClick: (wallpaper: Wallpaper) -> Unit = {},
    onWallpaperFavoriteClick: (wallpaper: Wallpaper) -> Unit = {},
    onFullWallpaperTransform: () -> Unit = {},
    onFullWallpaperTap: () -> Unit = {},
    onFullWallpaperInfoClick: () -> Unit = {},
    onFullWallpaperInfoDismiss: () -> Unit = {},
    onFullWallpaperShareLinkClick: () -> Unit = {},
    onFullWallpaperShareImageClick: () -> Unit = {},
    onFullWallpaperApplyWallpaperClick: () -> Unit = {},
    onFullWallpaperFullScreenClick: () -> Unit = {},
    onFullWallpaperTagClick: (WallhavenTag) -> Unit = {},
    onFullWallpaperUploaderClick: (WallhavenUploader) -> Unit = {},
    onFullWallpaperDownloadPermissionsGranted: () -> Unit = {},
    onFullWallpaperDownloadAllPermissionsGranted: () -> Unit = {},
    onFullWallpaperLightDarkTypeFlagsChange: (Int) -> Unit = {},
    fullWallpaperGalleryWallpapers: List<Wallpaper>? = null,
    fullWallpaperGalleryPageIndex: Int = 0,
    onFullWallpaperGalleryPageChange: (Int) -> Unit = {},
    fullWallpaperShowGalleryFavDialog: Boolean = false,
    onFullWallpaperGalleryFavScopeSelected: (all: Boolean) -> Unit = {},
    onFullWallpaperGalleryFavScopeDismiss: () -> Unit = {},
    showFullWallpaperTelegramAction: Boolean = false,
    onFullWallpaperPostToTelegramClick: () -> Unit = {},
) {
    CollectionsScreenContent(
        modifier = modifier,
        isExpanded = isExpanded,
        listContent = {
            var showFilterSheet by remember { mutableStateOf(false) }
            val activeFilterCount = remember(
                selectedSourceFilter,
                selectedPurityFilter,
                selectedAppearanceFilter,
                selectedDateFilter,
            ) {
                listOf(
                    selectedSourceFilter != null,
                    selectedPurityFilter != PurityFilter.ALL,
                    selectedAppearanceFilter != AppearanceFilter.ALL,
                    selectedDateFilter != null,
                ).count { it }
            }
            Column(modifier = Modifier.fillMaxSize()) {
                // Categories + Filters chip row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CategoriesRow(
                        modifier = Modifier.weight(1f),
                        selected = selectedCategory,
                        onCategoryClick = onCategoryClick,
                    )
                    FiltersChip(
                        activeFilterCount = activeFilterCount,
                        onClick = { showFilterSheet = true },
                    )
                }
                if (showFilterSheet) {
                    CollectionsFilterSheet(
                        selectedSource = selectedSourceFilter,
                        selectedPurityFilter = selectedPurityFilter,
                        selectedAppearanceFilter = selectedAppearanceFilter,
                        selectedDateFilter = selectedDateFilter,
                        showDateSeparators = showDateSeparators,
                        availableDates = availableDates,
                        isFavoritesCategory = selectedCategory == CollectionCategory.FAVORITES,
                        onDismissRequest = { showFilterSheet = false },
                        onApply = { s, p, a, d, sep -> onApplyFilters(s, p, a, d, sep) },
                    )
                }

                // Wallpapers Feed
                CollectionsFeedGrid(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("collections:feed"),
                    contentPadding = contentPadding,
                    feedItems = feedItems,
                    favorites = favorites,
                    viewedList = viewedList,
                    viewedWallpapersLook = viewedWallpapersLook,
                    blurSketchy = blurSketchy,
                    blurNsfw = blurNsfw,
                    selectedWallpaper = selectedWallpaper,
                    showSelection = showSelection,
                    layoutPreferences = layoutPreferences,
                    lightDarkList = lightDarkList,
                    selectedCategory = selectedCategory,
                    onWallpaperClick = onWallpaperClick,
                    onWallpaperLongClick = onWallpaperLongClick,
                    onWallpaperFavoriteClick = onWallpaperFavoriteClick,
                )
            }
        },
        detailContent = {
            WallpaperViewer(
                wallpaper = fullWallpaper,
                actionsVisible = fullWallpaperActionsVisible,
                downloadStatus = fullWallpaperDownloadStatus,
                loading = fullWallpaperLoading,
                thumbData = selectedWallpaper?.thumbData,
                isExpanded = true,
                showInfo = showFullWallpaperInfo,
                isFavorite = isFullWallpaperFavorite,
                onWallpaperTransform = onFullWallpaperTransform,
                onWallpaperTap = onFullWallpaperTap,
                onInfoClick = onFullWallpaperInfoClick,
                onInfoDismiss = onFullWallpaperInfoDismiss,
                onShareLinkClick = onFullWallpaperShareLinkClick,
                onShareImageClick = onFullWallpaperShareImageClick,
                onApplyWallpaperClick = onFullWallpaperApplyWallpaperClick,
                onFullScreenClick = onFullWallpaperFullScreenClick,
                onTagClick = onFullWallpaperTagClick,
                onUploaderClick = onFullWallpaperUploaderClick,
                onDownloadPermissionsGranted = onFullWallpaperDownloadPermissionsGranted,
                onDownloadAllPermissionsGranted = onFullWallpaperDownloadAllPermissionsGranted,
                onFavoriteToggle = {
                    if (fullWallpaper != null) {
                        onWallpaperFavoriteClick(fullWallpaper)
                    }
                },
                onLightDarkTypeFlagsChange = onFullWallpaperLightDarkTypeFlagsChange,
                galleryWallpapers = fullWallpaperGalleryWallpapers,
                galleryPageIndex = fullWallpaperGalleryPageIndex,
                onGalleryPageChange = onFullWallpaperGalleryPageChange,
                showGalleryFavScopeDialog = fullWallpaperShowGalleryFavDialog,
                onGalleryFavScopeSelected = onFullWallpaperGalleryFavScopeSelected,
                onGalleryFavScopeDismiss = onFullWallpaperGalleryFavScopeDismiss,
                showTelegramAction = showFullWallpaperTelegramAction,
                onPostToTelegramClick = onFullWallpaperPostToTelegramClick,
            )
        },
    )

    // ── Quick-actions sheet (long-press) ─────────────────────────────────
    quickActionsWallpaper?.let { wallpaper ->
        val isFav = favorites.any { it.sourceId == wallpaper.id && it.source == wallpaper.source }
        WallpaperQuickActionsSheet(
            wallpaper = wallpaper,
            isFavorite = isFav,
            showTelegram = showTelegram,
            onDismiss = onQuickActionsDismiss,
            onFavoriteClick = { onQuickActionsFavoriteClick(wallpaper) },
            onApplyWallpaperClick = { onQuickActionsApplyWallpaperClick(wallpaper) },
            onDownloadClick = { onQuickActionsDownloadClick(wallpaper) },
            onShareLinkClick = { onQuickActionsShareClick(wallpaper) },
            onPostToTelegramClick = { onQuickActionsTelegramClick(wallpaper) },
        )
    }
}

@Composable
private fun CollectionsScreenContent(
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false,
    listContent: @Composable () -> Unit = {},
    detailContent: @Composable () -> Unit = {},
) {
    val listSaveableStateHolder = rememberSaveableStateHolder()
    val list = remember {
        movableContentOf {
            listSaveableStateHolder.SaveableStateProvider(0) {
                listContent()
            }
        }
    }

    Box(
        modifier = modifier,
    ) {
        if (isExpanded) {
            BottomBarAwareHorizontalTwoPane(
                modifier = Modifier.fillMaxSize(),
                first = list,
                second = detailContent,
                splitFraction = 0.5f,
            )
        } else {
            list()
        }
    }
}

// ── Feed grid that handles both WallpaperItem and DateHeader ─────────────

@Composable
private fun CollectionsFeedGrid(
    feedItems: LazyPagingItems<CollectionsFeedItem>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(8.dp),
    layoutPreferences: LayoutPreferences = LayoutPreferences(),
    favorites: ImmutableList<Favorite> = persistentListOf(),
    viewedList: ImmutableList<Viewed> = persistentListOf(),
    viewedWallpapersLook: ViewedWallpapersLook = ViewedWallpapersLook.DIM_WITH_LABEL,
    lightDarkList: ImmutableList<LightDark> = persistentListOf(),
    blurSketchy: Boolean = false,
    blurNsfw: Boolean = false,
    showSelection: Boolean = false,
    selectedWallpaper: Wallpaper? = null,
    selectedCategory: CollectionCategory = CollectionCategory.FAVORITES,
    onWallpaperClick: (wallpaper: Wallpaper) -> Unit = {},
    onWallpaperLongClick: (wallpaper: Wallpaper) -> Unit = {},
    onWallpaperFavoriteClick: (wallpaper: Wallpaper) -> Unit = {},
) {
    val isRefreshing = feedItems.loadState.refresh == LoadState.Loading
    var gridSize by rememberSaveable(
        stateSaver = IntSize.Saver,
    ) { mutableStateOf(IntSize.Zero) }
    val gridWidthDp = gridSize.width.toDp()
    val layoutDirection = LocalLayoutDirection.current
    val adaptiveMinWidth = getAdaptiveMinWidth(
        layoutPreferences.gridColType,
        contentPadding,
        layoutDirection,
        gridWidthDp,
        layoutPreferences.gridColMinWidthPct,
    )
    val itemSpacingDp = 6

    LazyVerticalStaggeredGrid(
        modifier = modifier.onSizeChanged { gridSize = it },
        state = feedItems.rememberLazyStaggeredGridState(),
        contentPadding = contentPadding,
        columns = when (layoutPreferences.gridColType) {
            GridColType.ADAPTIVE -> StaggeredGridCells.Adaptive(minSize = adaptiveMinWidth)
            GridColType.FIXED -> StaggeredGridCells.Fixed(count = layoutPreferences.gridColCount)
        },
        verticalItemSpacing = itemSpacingDp.dp,
        horizontalArrangement = Arrangement.spacedBy(itemSpacingDp.dp),
    ) {
        if (feedItems.itemCount == 0 && !isRefreshing) {
            item(span = StaggeredGridItemSpan.FullLine) {
                Text(
                    modifier = Modifier.padding(vertical = 100.dp),
                    text = stringResource(
                        when (selectedCategory) {
                            CollectionCategory.FAVORITES -> R.string.no_favorites
                            CollectionCategory.LIGHT_DARK -> R.string.no_light_dark
                        },
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    textAlign = TextAlign.Center,
                )
            }
        }
        if (isRefreshing && feedItems.itemCount == 0) {
            items(9) { PlaceholderWallpaperCard() }
            return@LazyVerticalStaggeredGrid
        }
        items(
            count = feedItems.itemCount,
            span = { index ->
                when (feedItems.peek(index)) {
                    is CollectionsFeedItem.DateHeader -> StaggeredGridItemSpan.FullLine
                    else -> StaggeredGridItemSpan.SingleLane
                }
            },
            key = feedItems.itemKey { item ->
                when (item) {
                    is CollectionsFeedItem.WallpaperItem -> "w_${item.wallpaper.id}"
                    is CollectionsFeedItem.DateHeader -> "d_${item.date}"
                }
            },
            contentType = feedItems.itemContentType { item ->
                when (item) {
                    is CollectionsFeedItem.WallpaperItem -> "wallpaper"
                    is CollectionsFeedItem.DateHeader -> "dateHeader"
                }
            },
        ) { index ->
            when (val item = feedItems[index]) {
                is CollectionsFeedItem.WallpaperItem -> {
                    val wallpaper = item.wallpaper
                    WallpaperCard(
                        modifier = Modifier.animateItem(),
                        wallpaper = wallpaper,
                        blur = when (wallpaper.purity) {
                            Purity.SFW -> false
                            Purity.SKETCHY -> blurSketchy
                            Purity.NSFW -> blurNsfw
                        },
                        isSelected = showSelection && selectedWallpaper?.id == wallpaper.id,
                        isFavorite = favorites.find { f ->
                            f.sourceId == wallpaper.id && f.source == wallpaper.source
                        } != null,
                        fixedHeight = false,
                        roundedCorners = layoutPreferences.roundedCorners,
                        isViewed = viewedList.find { v ->
                            v.sourceId == wallpaper.id && v.source == wallpaper.source
                        } != null,
                        viewedWallpapersLook = viewedWallpapersLook,
                        lightDarkTypeFlags = lightDarkList.find { v ->
                            v.sourceId == wallpaper.id && v.source == wallpaper.source
                        }?.typeFlags ?: LightDarkType.UNSPECIFIED,
                        isGalleryCover = layoutPreferences.showCarousel &&
                            wallpaper is RedditWallpaper &&
                            wallpaper.galleryPosition == 0,
                        onClick = { onWallpaperClick(wallpaper) },
                        onLongClick = { onWallpaperLongClick(wallpaper) },
                        onFavoriteClick = { onWallpaperFavoriteClick(wallpaper) },
                    )
                }
                is CollectionsFeedItem.DateHeader -> {
                    DateSeparatorItem(
                        modifier = Modifier.animateItem(),
                        date = item.date,
                    )
                }
                null -> PlaceholderWallpaperCard()
            }
        }
    }
}

@Composable
private fun DateSeparatorItem(
    date: LocalDate,
    modifier: Modifier = Modifier,
) {
    val today = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }
    val yesterday = remember(today) { today.minus(1, DateTimeUnit.DAY) }
    val todayStr = stringResource(R.string.today)
    val yesterdayStr = stringResource(R.string.yesterday)
    val formattedDate = remember(date) {
        java.time.LocalDate.of(date.year, date.monthNumber, date.dayOfMonth)
            .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
    }
    val label = when (date) {
        today -> todayStr
        yesterday -> yesterdayStr
        else -> formattedDate
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}
