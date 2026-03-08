package com.ammar.wallflow.ui.wallpaperviewer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ammar.wallflow.data.repository.AppPreferencesRepository
import com.ammar.wallflow.data.repository.FavoritesRepository
import com.ammar.wallflow.data.repository.LightDarkRepository
import com.ammar.wallflow.data.repository.ViewedRepository
import com.ammar.wallflow.data.repository.local.LocalWallpapersRepository
import com.ammar.wallflow.data.repository.reddit.RedditRepository
import com.ammar.wallflow.data.repository.utils.Resource
import com.ammar.wallflow.data.repository.utils.successOr
import com.ammar.wallflow.data.repository.wallhaven.WallhavenRepository
import com.ammar.wallflow.model.DownloadableWallpaper
import com.ammar.wallflow.model.LightDarkType
import com.ammar.wallflow.model.Source
import com.ammar.wallflow.model.Wallpaper
import com.ammar.wallflow.model.reddit.RedditWallpaper
import com.ammar.wallflow.model.wallhaven.WallhavenWallpaper
import com.ammar.wallflow.utils.DownloadManager
import com.ammar.wallflow.utils.DownloadStatus
import com.ammar.wallflow.utils.ExifWriteType
import com.ammar.wallflow.utils.combine
import com.github.materiiapps.partial.Partialize
import com.github.materiiapps.partial.getOrElse
import com.github.materiiapps.partial.partial
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class WallpaperViewerViewModel @Inject constructor(
    private val application: Application,
    private val wallhavenRepository: WallhavenRepository,
    private val redditRepository: RedditRepository,
    private val localWallpapersRepository: LocalWallpapersRepository,
    private val downloadManager: DownloadManager,
    private val favoritesRepository: FavoritesRepository,
    private val appPreferencesRepository: AppPreferencesRepository,
    private val viewedRepository: ViewedRepository,
    private val lightDarkRepository: LightDarkRepository,
) : AndroidViewModel(
    application = application,
) {
    private val localUiState = MutableStateFlow(WallpaperViewerUiStatePartial())
    private val argsFlow = MutableStateFlow(WallpaperViewerArgs())
    private val galleryPageIndexFlow = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val wallpaperFlow = argsFlow.flatMapLatest {
        if (it.source == null || it.wallpaperId == null) {
            flowOf(Resource.Success(null))
        } else {
            flow<Resource<Wallpaper?>> {
                emit(Resource.Success(null))
                emitAll(
                    when (it.source) {
                        Source.WALLHAVEN -> wallhavenRepository.wallpaper(it.wallpaperId)
                        Source.REDDIT -> redditRepository.wallpaper(it.wallpaperId)
                        Source.LOCAL -> localWallpapersRepository.wallpaper(
                            application,
                            it.wallpaperId,
                        )
                    },
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Resource.Loading(null),
    )

    // Gallery wallpapers: loaded when a Reddit gallery image is opened with carousel enabled
    @OptIn(ExperimentalCoroutinesApi::class)
    private val galleryWallpapersFlow = kotlinx.coroutines.flow.combine(
        wallpaperFlow,
        appPreferencesRepository.appPreferencesFlow,
    ) { wp, prefs -> wp to prefs }.flatMapLatest { (wallpaperResource, prefs) ->
        val wallpaper = wallpaperResource.successOr(null)
        val showCarousel = prefs.lookAndFeelPreferences.layoutPreferences.showCarousel
        if (wallpaper == null || !showCarousel || wallpaper !is RedditWallpaper ||
            wallpaper.galleryPosition == null
        ) {
            flowOf<List<Wallpaper>?>(null)
        } else {
            flow {
                val siblings = redditRepository.galleryWallpapers(wallpaper.postId)
                emit(if (siblings.size <= 1) null else siblings)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null,
    )

    // Active wallpaper = the wallpaper on the current gallery page, or the single wallpaper
    private val activeWallpaperFlow = kotlinx.coroutines.flow.combine(
        wallpaperFlow,
        galleryWallpapersFlow,
        galleryPageIndexFlow,
    ) { wallpaperResource, gallery, pageIndex ->
        gallery?.getOrNull(pageIndex)?.let { Resource.Success(it) } ?: wallpaperResource
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Resource.Loading(null),
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val isFavoriteFlow = activeWallpaperFlow.flatMapLatest {
        val wallpaper = it.successOr(null) ?: return@flatMapLatest flowOf(false)
        return@flatMapLatest favoritesRepository.observeIsFavorite(
            source = wallpaper.source,
            sourceId = wallpaper.id,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false,
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val lightDarkTypeFlags = activeWallpaperFlow.flatMapLatest {
        val wallpaper = it.successOr(null) ?: return@flatMapLatest flowOf(LightDarkType.UNSPECIFIED)
        lightDarkRepository.observeTypeFlags(
            source = wallpaper.source,
            sourceId = wallpaper.id,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LightDarkType.UNSPECIFIED,
    )

    val uiState = combine(
        localUiState,
        activeWallpaperFlow,
        argsFlow,
        isFavoriteFlow,
        appPreferencesRepository.appPreferencesFlow,
        lightDarkTypeFlags,
        galleryWallpapersFlow,
        galleryPageIndexFlow,
    ) {
            local,
            wallpaper,
            args,
            isFavorite,
            appPreferences,
            lightDarkTypeFlags,
            galleryWallpapers,
            galleryPageIndex,
        ->
        local.merge(
            WallpaperViewerUiState(
                wallpaper = wallpaper.successOr(null),
                thumbData = args.thumbData,
                loading = wallpaper is Resource.Loading,
                isFavorite = isFavorite,
                writeTagsToExif = appPreferences.writeTagsToExif,
                tagsExifWriteType = appPreferences.tagsExifWriteType,
                rememberViewedWallpapers = appPreferences.viewedWallpapersPreferences.enabled,
                lightDarkTypeFlags = lightDarkTypeFlags ?: LightDarkType.UNSPECIFIED,
                telegramEnabled = appPreferences.telegramPreferences.enabled,
                telegramIsConfigured = appPreferences.telegramPreferences.isConfigured,
                galleryWallpapers = galleryWallpapers,
                galleryPageIndex = galleryPageIndex,
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WallpaperViewerUiState(),
    )

    init {
        // When the primary wallpaper changes, seed the page index from its galleryPosition
        // so opening any gallery page (not just the cover) lands on the right page.
        viewModelScope.launch {
            wallpaperFlow.collectLatest { resource ->
                val wp = resource.successOr(null)
                galleryPageIndexFlow.value = (wp as? RedditWallpaper)?.galleryPosition ?: 0
            }
        }
        viewModelScope.launch {
            combine(
                activeWallpaperFlow,
                appPreferencesRepository.appPreferencesFlow,
            ) { wallpaper, appPreferences ->
                wallpaper to appPreferences.viewedWallpapersPreferences.enabled
            }.collectLatest {
                val (wallpaperResource, rememberViewed) = it
                if (!rememberViewed) {
                    return@collectLatest
                }
                val wallpaper = wallpaperResource.successOr(null) ?: return@collectLatest
                viewedRepository.upsert(
                    sourceId = wallpaper.id,
                    source = wallpaper.source,
                )
            }
        }
    }

    fun setWallpaper(
        source: Source,
        wallpaperId: String?,
        thumbData: String?,
    ) = argsFlow.update {
        WallpaperViewerArgs(
            source = source,
            wallpaperId = wallpaperId,
            thumbData = thumbData,
        )
    }

    fun onWallpaperTap() = localUiState.update {
        it.copy(
            actionsVisible = partial(!it.actionsVisible.getOrElse { true }),
        )
    }

    // fun onWallpaperTransform() = localUiState.update {
    //     it.copy(
    //         actionsVisible = partial(false),
    //     )
    // }
    fun onWallpaperTransform() {}

    fun showInfo(show: Boolean = true) = localUiState.update {
        it.copy(showInfo = partial(show))
    }

    /** Called from the full wallpaper viewer – wallpaper is already in [uiState]. */
    fun postToTelegram() = postToTelegramInternal(uiState.value.wallpaper)

    /**
     * Called from quick-action handlers that already have the [Wallpaper] object.
     * Using an overload instead of a default parameter keeps the no-arg [postToTelegram]
     * usable as a `() -> Unit` function reference in [WallpaperScreen].
     */
    fun postToTelegram(wallpaper: Wallpaper) = postToTelegramInternal(wallpaper)

    private fun postToTelegramInternal(wallpaper: Wallpaper?) {
        if (wallpaper == null || wallpaper !is DownloadableWallpaper) return
        viewModelScope.launch {
            val state = uiState.value
            val tags = if (state.writeTagsToExif && wallpaper is WallhavenWallpaper) {
                wallpaper.tags?.map { it.name }
            } else {
                null
            }
            downloadManager.requestDownload(
                context = application,
                wallpaper = wallpaper,
                tags = tags,
                tagsExifWriteType = state.tagsExifWriteType,
                postToTelegram = true,
            )
        }
    }

    fun download() {
        download(uiState.value.wallpaper ?: return)
    }

    fun downloadAll() {
        val wallpapers = uiState.value.galleryWallpapers
            ?.filterIsInstance<DownloadableWallpaper>()
            ?.filterIsInstance<Wallpaper>()
        if (!wallpapers.isNullOrEmpty()) {
            wallpapers.forEach { download(it) }
        } else {
            download()
        }
    }

    /**
     * Downloads [wallpaper] directly without touching [argsFlow]. Safe to call from quick-action
     * handlers that already hold the target wallpaper (e.g. grid long-press) so the open viewer
     * state is never disrupted.
     */
    fun download(wallpaper: Wallpaper) {
        if (wallpaper !is DownloadableWallpaper) return
        var job: Job? = null
        job = viewModelScope.launch {
            val uiState = uiState.value
            val tags = if (uiState.writeTagsToExif && wallpaper is WallhavenWallpaper) {
                wallpaper.tags?.map { it.name }
            } else {
                null
            }
            val workName = downloadManager.requestDownload(
                context = application,
                wallpaper = wallpaper,
                tags = tags,
                tagsExifWriteType = uiState.tagsExifWriteType,
            )
            downloadManager.getProgress(
                context = application,
                workName = workName,
            ).collectLatest { state ->
                localUiState.update { it.copy(downloadStatus = partial(state)) }
                if (state.isSuccessOrFail()) {
                    job?.cancel()
                }
            }
        }
    }

    fun downloadForSharing(onResult: (file: File?) -> Unit) {
        downloadForSharing(uiState.value.wallpaper ?: return, onResult)
    }

    /**
     * Downloads [wallpaper] for sharing/applying without touching [argsFlow]. Safe to call from
     * quick-action handlers that already hold the target wallpaper.
     */
    fun downloadForSharing(wallpaper: Wallpaper, onResult: (file: File?) -> Unit) {
        if (wallpaper !is DownloadableWallpaper) return
        var job: Job? = null
        job = viewModelScope.launch {
            downloadManager.downloadWallpaperAsync(
                context = application,
                wallpaper = wallpaper,
                onLoadingChange = { loading ->
                    localUiState.update { it.copy(loading = partial(loading)) }
                },
                onResult = {
                    onResult(it)
                    job?.cancel()
                },
            )
        }
    }

    fun toggleFavorite() = viewModelScope.launch {
        val state = uiState.value
        val gallery = state.galleryWallpapers
        if (gallery != null && gallery.size > 1) {
            // Show scope-selection dialog instead of acting immediately.
            localUiState.update { it.copy(showGalleryFavDialog = partial(true)) }
        } else {
            val wallpaper = state.wallpaper ?: return@launch
            favoritesRepository.toggleFavorite(
                sourceId = wallpaper.id,
                source = wallpaper.source,
            )
        }
    }

    fun dismissGalleryFavDialog() = localUiState.update {
        it.copy(showGalleryFavDialog = partial(false))
    }

    /** Toggle favorite for the active gallery page only, or for every image in the gallery. */
    fun toggleFavoriteScope(all: Boolean) = viewModelScope.launch {
        dismissGalleryFavDialog()
        val state = uiState.value
        val gallery = state.galleryWallpapers
        if (all && gallery != null && gallery.size > 1) {
            val isFav = state.isFavorite
            gallery.forEach { gWp ->
                if (isFav) favoritesRepository.removeFavorite(gWp.id, gWp.source)
                else favoritesRepository.addFavorite(gWp.id, gWp.source)
            }
        } else {
            val wallpaper = gallery?.getOrNull(state.galleryPageIndex)
                ?: state.wallpaper ?: return@launch
            favoritesRepository.toggleFavorite(
                sourceId = wallpaper.id,
                source = wallpaper.source,
            )
        }
    }

    fun updateLightDarkTypeFlags(flags: Int) = viewModelScope.launch {
        val wallpaper = uiState.value.wallpaper ?: return@launch
        lightDarkRepository.upsert(
            sourceId = wallpaper.id,
            source = wallpaper.source,
            typeFlags = flags,
        )
    }

    fun setGalleryPage(index: Int) {
        galleryPageIndexFlow.value = index
    }

    /** Synchronous read of the current gallery page index — safe to call from action callbacks. */
    val currentGalleryPage: Int get() = galleryPageIndexFlow.value
}

@Partialize
data class WallpaperViewerUiState(
    val wallpaper: Wallpaper? = null,
    val thumbData: String? = null,
    val actionsVisible: Boolean = true,
    val showInfo: Boolean = false,
    val downloadStatus: DownloadStatus? = null,
    val loading: Boolean = true,
    val isFavorite: Boolean = false,
    val showGalleryFavDialog: Boolean = false,
    val writeTagsToExif: Boolean = false,
    val tagsExifWriteType: ExifWriteType = ExifWriteType.APPEND,
    val rememberViewedWallpapers: Boolean = false,
    val lightDarkTypeFlags: Int = LightDarkType.UNSPECIFIED,
    val telegramEnabled: Boolean = false,
    val telegramIsConfigured: Boolean = false,
    val galleryWallpapers: List<Wallpaper>? = null,
    val galleryPageIndex: Int = 0,
)

data class WallpaperViewerArgs(
    val source: Source? = null,
    val wallpaperId: String? = null,
    val thumbData: String? = null,
)
