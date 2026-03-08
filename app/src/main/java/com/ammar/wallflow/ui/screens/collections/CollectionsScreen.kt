package com.ammar.wallflow.ui.screens.collections

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import com.ammar.wallflow.R
import com.ammar.wallflow.destinations.WallpaperScreenDestination
import com.ammar.wallflow.extensions.search
import com.ammar.wallflow.model.CollectionCategory
import com.ammar.wallflow.model.Source
import com.ammar.wallflow.model.Wallpaper
import com.ammar.wallflow.ui.screens.collections.AppearanceFilter
import com.ammar.wallflow.model.search.WallhavenTagSearchMeta
import com.ammar.wallflow.model.search.WallhavenUploaderSearchMeta
import com.ammar.wallflow.model.wallhaven.WallhavenTag
import com.ammar.wallflow.model.wallhaven.WallhavenUploader
import com.ammar.wallflow.navigation.AppNavGraphs.CollectionsNavGraph
import com.ammar.wallflow.ui.common.LocalSystemController
import com.ammar.wallflow.ui.common.bottomWindowInsets
import com.ammar.wallflow.ui.common.bottombar.LocalBottomBarController
import com.ammar.wallflow.ui.common.mainsearch.MainSearchBar
import com.ammar.wallflow.ui.common.topWindowInsets
import com.ammar.wallflow.ui.screens.main.RootNavControllerWrapper
import com.ammar.wallflow.ui.wallpaperviewer.WallpaperViewerViewModel
import com.ammar.wallflow.utils.applyWallpaper
import com.ammar.wallflow.utils.getStartBottomPadding
import com.ammar.wallflow.utils.shareWallpaper
import com.ammar.wallflow.utils.shareWallpaperUrl
import com.ramcosta.composedestinations.annotation.Destination

@Destination<CollectionsNavGraph>(
    start = true,
)
@Composable
fun CollectionsScreen(
    navController: NavController,
    rootNavControllerWrapper: RootNavControllerWrapper,
    viewModel: CollectionsViewModel = hiltViewModel(),
    viewerViewModel: WallpaperViewerViewModel = hiltViewModel(),
) {
    val rootNavController = rootNavControllerWrapper.navController
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val viewerUiState by viewerViewModel.uiState.collectAsStateWithLifecycle()
    val wallpapers = viewModel.feedItems.collectAsLazyPagingItems()
    val context = LocalContext.current
    val systemController = LocalSystemController.current
    val bottomBarController = LocalBottomBarController.current
    val systemState by systemController.state
    val bottomWindowInsets = bottomWindowInsets
    val navigationBarsInsets = WindowInsets.navigationBars
    val density = LocalDensity.current
    val bottomPadding = remember(
        bottomBarController.state.value,
        density,
        bottomWindowInsets.getBottom(density),
        navigationBarsInsets.getBottom(density),
    ) {
        getStartBottomPadding(
            density,
            bottomBarController,
            bottomWindowInsets,
            navigationBarsInsets,
        )
    }

    LaunchedEffect(Unit) {
        systemController.resetBarsState()
        bottomBarController.update { it.copy(visible = true) }
    }

    val onWallpaperClick: (wallpaper: Wallpaper) -> Unit = remember(systemState.isExpanded) {
        {
            if (systemState.isExpanded) {
                viewModel.setSelectedWallpaper(it)
                viewerViewModel.setWallpaper(
                    source = it.source,
                    wallpaperId = it.id,
                    thumbData = it.thumbData,
                )
            } else {
                // navigate to wallpaper screen
                rootNavController.navigate(
                    WallpaperScreenDestination(
                        source = it.source,
                        wallpaperId = it.id,
                        thumbData = it.thumbData,
                    ).route,
                )
            }
        }
    }

    val onTagClick: (wallhavenTag: WallhavenTag) -> Unit = remember(
        uiState.prevMainWallhavenSearch,
    ) {
        fn@{
            val prevSearch = uiState.prevMainWallhavenSearch
                ?: MainSearchBar.Defaults.wallhavenSearch
            val search = prevSearch.copy(
                query = "id:${it.id}",
                meta = WallhavenTagSearchMeta(it),
            )
            navController.search(search)
        }
    }

    val onUploaderClick: (WallhavenUploader) -> Unit = remember(
        uiState.prevMainWallhavenSearch,
    ) {
        fn@{
            val prevSearch = uiState.prevMainWallhavenSearch
                ?: MainSearchBar.Defaults.wallhavenSearch
            val search = prevSearch.copy(
                query = "@${it.username}",
                meta = WallhavenUploaderSearchMeta(uploader = it),
            )
            navController.search(search)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(topWindowInsets),
    ) {
        CollectionsScreenContent(
            modifier = Modifier.fillMaxSize(),
            isExpanded = systemState.isExpanded,
            contentPadding = PaddingValues(
                start = if (systemState.isExpanded) 0.dp else 8.dp,
                end = if (systemState.isExpanded) 0.dp else 8.dp,
                top = 8.dp,
                bottom = bottomPadding + 8.dp,
            ),
            feedItems = wallpapers,
            favorites = uiState.favorites,
            viewedList = uiState.viewedList,
            viewedWallpapersLook = uiState.viewedWallpapersLook,
            lightDarkList = uiState.lightDarkList,
            blurSketchy = uiState.blurSketchy,
            blurNsfw = uiState.blurNsfw,
            selectedWallpaper = uiState.selectedWallpaper,
            showSelection = systemState.isExpanded,
            layoutPreferences = uiState.layoutPreferences,
            selectedCategory = uiState.selectedCategory,
            selectedSourceFilter = uiState.selectedSourceFilter,
            selectedPurityFilter = uiState.selectedPurityFilter,
            selectedAppearanceFilter = uiState.selectedAppearanceFilter,
            selectedDateFilter = uiState.selectedDateFilter,
            showDateSeparators = uiState.showDateSeparators,
            availableDates = uiState.availableDates,
            onCategoryClick = viewModel::changeCategory,
            onApplyFilters = { source, purity, appearance, date, showSep ->
                viewModel.setSourceFilter(source)
                viewModel.setPurityFilter(purity)
                viewModel.setAppearanceFilter(appearance)
                viewModel.setDateFilter(date)
                viewModel.setShowDateSeparators(showSep)
            },
            quickActionsWallpaper = uiState.quickActionsWallpaper,
            onWallpaperLongClick = viewModel::setQuickActionsWallpaper,
            onQuickActionsDismiss = { viewModel.setQuickActionsWallpaper(null) },
            onQuickActionsFavoriteClick = viewModel::toggleFavorite,
            onQuickActionsApplyWallpaperClick = { wallpaper ->
                applyWallpaper(context, viewerViewModel, wallpaper)
                viewModel.setQuickActionsWallpaper(null)
            },
            onQuickActionsDownloadClick = { wallpaper ->
                viewerViewModel.download(wallpaper)
                viewModel.setQuickActionsWallpaper(null)
            },
            onQuickActionsShareClick = { wallpaper ->
                shareWallpaperUrl(context, wallpaper)
                viewModel.setQuickActionsWallpaper(null)
            },
            showTelegram = uiState.telegramIsConfigured,
            onQuickActionsTelegramClick = { wallpaper ->
                viewerViewModel.postToTelegram(wallpaper)
                viewModel.setQuickActionsWallpaper(null)
            },
            fullWallpaper = viewerUiState.wallpaper,
            fullWallpaperActionsVisible = viewerUiState.actionsVisible,
            fullWallpaperDownloadStatus = viewerUiState.downloadStatus,
            fullWallpaperLoading = viewerUiState.loading,
            showFullWallpaperInfo = viewerUiState.showInfo,
            isFullWallpaperFavorite = viewerUiState.isFavorite,
            onWallpaperClick = onWallpaperClick,
            onWallpaperFavoriteClick = viewModel::toggleFavorite,
            onFullWallpaperTransform = viewerViewModel::onWallpaperTransform,
            onFullWallpaperTap = viewerViewModel::onWallpaperTap,
            onFullWallpaperInfoClick = viewerViewModel::showInfo,
            onFullWallpaperInfoDismiss = { viewerViewModel.showInfo(false) },
            onFullWallpaperShareLinkClick = {
                val wallpaper = viewerUiState.wallpaper ?: return@CollectionsScreenContent
                shareWallpaperUrl(context, wallpaper)
            },
            onFullWallpaperShareImageClick = {
                val wallpaper = viewerUiState.wallpaper ?: return@CollectionsScreenContent
                shareWallpaper(context, viewerViewModel, wallpaper)
            },
            onFullWallpaperApplyWallpaperClick = {
                val wallpaper = viewerUiState.galleryWallpapers?.getOrNull(viewerViewModel.currentGalleryPage)
                    ?: viewerUiState.wallpaper ?: return@CollectionsScreenContent
                applyWallpaper(context, viewerViewModel, wallpaper)
            },
            onFullWallpaperFullScreenClick = {
                viewerUiState.wallpaper?.run {
                    rootNavController.navigate(
                        WallpaperScreenDestination(
                            source = source,
                            thumbData = thumbData,
                            wallpaperId = id,
                        ).route,
                    )
                }
            },
            onFullWallpaperTagClick = onTagClick,
            onFullWallpaperUploaderClick = onUploaderClick,
            onFullWallpaperDownloadPermissionsGranted = viewerViewModel::download,
            onFullWallpaperDownloadAllPermissionsGranted = viewerViewModel::downloadAll,
            onFullWallpaperLightDarkTypeFlagsChange = viewerViewModel::updateLightDarkTypeFlags,
            fullWallpaperGalleryWallpapers = viewerUiState.galleryWallpapers,
            fullWallpaperGalleryPageIndex = viewerUiState.galleryPageIndex,
            onFullWallpaperGalleryPageChange = viewerViewModel::setGalleryPage,
            fullWallpaperShowGalleryFavDialog = viewerUiState.showGalleryFavDialog,
            onFullWallpaperGalleryFavScopeSelected = viewerViewModel::toggleFavoriteScope,
            onFullWallpaperGalleryFavScopeDismiss = viewerViewModel::dismissGalleryFavDialog,
            showFullWallpaperTelegramAction = viewerUiState.telegramEnabled && viewerUiState.telegramIsConfigured,
            onFullWallpaperPostToTelegramClick = {
                val wallpaper = viewerUiState.galleryWallpapers?.getOrNull(viewerViewModel.currentGalleryPage)
                    ?: viewerUiState.wallpaper ?: return@CollectionsScreenContent
                viewerViewModel.postToTelegram(wallpaper)
            },
        )
    }
}
