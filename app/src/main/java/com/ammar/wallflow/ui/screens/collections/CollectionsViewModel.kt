package com.ammar.wallflow.ui.screens.collections

import android.app.Application
import androidx.compose.runtime.Stable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.insertSeparators
import androidx.paging.map
import com.ammar.wallflow.data.db.entity.FavoriteEntity
import com.ammar.wallflow.data.db.entity.LightDarkEntity
import com.ammar.wallflow.data.db.entity.ViewedEntity
import com.ammar.wallflow.data.db.entity.toFavorite
import com.ammar.wallflow.data.db.entity.toLightDark
import com.ammar.wallflow.data.db.entity.toViewed
import com.ammar.wallflow.data.preferences.AppPreferences
import com.ammar.wallflow.data.preferences.LayoutPreferences
import com.ammar.wallflow.data.preferences.ViewedWallpapersLook
import com.ammar.wallflow.data.repository.AppPreferencesRepository
import com.ammar.wallflow.data.repository.FavoritesRepository
import com.ammar.wallflow.data.repository.LightDarkRepository
import com.ammar.wallflow.data.repository.ViewedRepository
import com.ammar.wallflow.model.reddit.RedditWallpaper
import com.ammar.wallflow.model.CollectionCategory
import com.ammar.wallflow.model.Favorite
import com.ammar.wallflow.model.LightDark
import com.ammar.wallflow.model.LightDarkType
import com.ammar.wallflow.model.Purity
import com.ammar.wallflow.model.Source
import com.ammar.wallflow.model.isDark
import com.ammar.wallflow.model.isLight
import com.ammar.wallflow.model.Viewed
import com.ammar.wallflow.model.Wallpaper
import com.ammar.wallflow.model.search.RedditSearch
import com.ammar.wallflow.model.search.WallhavenSearch
import com.ammar.wallflow.utils.combine
import com.github.materiiapps.partial.Partialize
import com.github.materiiapps.partial.partial
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine as stdCombine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

enum class PurityFilter {
    ALL, SFW, NSFW
}

enum class AppearanceFilter {
    ALL, LIGHT, DARK
}

@HiltViewModel
class CollectionsViewModel @Inject constructor(
    application: Application,
    private val favoritesRepository: FavoritesRepository,
    private val lightDarkRepository: LightDarkRepository,
    private val appPreferencesRepository: AppPreferencesRepository,
    viewedRepository: ViewedRepository,
) : AndroidViewModel(
    application = application,
) {
    private val selectedCategoryFlow = MutableStateFlow(CollectionCategory.FAVORITES)
    private val selectedSourceFilterFlow = MutableStateFlow<Source?>(null)
    private val selectedPurityFilterFlow = MutableStateFlow(PurityFilter.ALL)
    private val selectedAppearanceFilterFlow = MutableStateFlow(AppearanceFilter.ALL)
    private val selectedDateFilterFlow = MutableStateFlow<LocalDate?>(null)
    private val localUiState = MutableStateFlow(CollectionsUiStatePartial())

    /** Combines all filter knobs into a single snapshot for the paging pipeline. */
    private data class FeedFilters(
        val sourceFilter: Source?,
        val purityFilter: PurityFilter,
        val appearanceFilter: AppearanceFilter,
        val dateFilter: LocalDate?,
        val showSep: Boolean,
        val ldList: List<LightDarkEntity>,
        val showCarousel: Boolean,
    )

    /** Pre-combined filter state used BOTH by the paging pipeline and the uiState combine. */
    private val filtersFlow: Flow<FeedFilters> = combine(
        selectedSourceFilterFlow,
        selectedPurityFilterFlow,
        selectedAppearanceFilterFlow,
        selectedDateFilterFlow,
        lightDarkRepository.observeAll(),
        appPreferencesRepository.appPreferencesFlow,
    ) { src, pur, app, date, ldList, prefs ->
        FeedFilters(
            sourceFilter = src,
            purityFilter = pur,
            appearanceFilter = app,
            dateFilter = date,
            showSep = prefs.lookAndFeelPreferences.layoutPreferences.showCollectionsDateSeparators,
            ldList = ldList,
            showCarousel = prefs.lookAndFeelPreferences.layoutPreferences.showCarousel,
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val feedItems: Flow<PagingData<CollectionsFeedItem>> =
        selectedCategoryFlow.flatMapLatest { category ->
            // Base pager — for FAVORITES we carry favoritedOn; for LIGHT_DARK use epoch-zero
            val baseFlow: Flow<PagingData<CollectionsFeedItem.WallpaperItem>> = when (category) {
                CollectionCategory.FAVORITES ->
                    favoritesRepository.wallpapersWithDatePager(context = application)
                        .map { pd ->
                            pd.map { (wp, ts) ->
                                CollectionsFeedItem.WallpaperItem(wp, ts)
                            }
                        }
                CollectionCategory.LIGHT_DARK ->
                    lightDarkRepository.wallpapersPager(context = application)
                        .map { pd ->
                            pd.map { wp ->
                                CollectionsFeedItem.WallpaperItem(
                                    wp,
                                    Instant.fromEpochMilliseconds(0L),
                                )
                            }
                        }
            }

            filtersFlow.flatMapLatest { filters ->
                val filtered: Flow<PagingData<CollectionsFeedItem.WallpaperItem>> =
                    baseFlow.map { pagingData ->
                        pagingData.filter { item ->
                            val wallpaper = item.wallpaper
                            val sourceMatches =
                                filters.sourceFilter == null || wallpaper.source == filters.sourceFilter
                            val purityMatches = when (filters.purityFilter) {
                                PurityFilter.ALL -> true
                                PurityFilter.SFW -> wallpaper.purity == Purity.SFW
                                PurityFilter.NSFW ->
                                    wallpaper.purity == Purity.NSFW ||
                                        wallpaper.purity == Purity.SKETCHY
                            }
                            val appearanceMatches = when (filters.appearanceFilter) {
                                AppearanceFilter.ALL -> true
                                AppearanceFilter.LIGHT -> {
                                    val ld = filters.ldList.find {
                                        it.sourceId == wallpaper.id && it.source == wallpaper.source
                                    }
                                    ld != null && ld.typeFlags.isLight()
                                }
                                AppearanceFilter.DARK -> {
                                    val ld = filters.ldList.find {
                                        it.sourceId == wallpaper.id && it.source == wallpaper.source
                                    }
                                    ld != null && ld.typeFlags.isDark()
                                }
                            }
                            val carouselMatches =
                                if (filters.showCarousel && wallpaper is RedditWallpaper) {
                                    wallpaper.galleryPosition == null || wallpaper.galleryPosition == 0
                                } else {
                                    true
                                }
                            val dateMatches =
                                if (filters.dateFilter != null &&
                                    category == CollectionCategory.FAVORITES
                                ) {
                                    item.localDate == filters.dateFilter
                                } else {
                                    true
                                }
                            sourceMatches && purityMatches && appearanceMatches &&
                                carouselMatches && dateMatches
                        }
                    }

                // Widen type to the sealed parent so insertSeparators can inject DateHeaders
                filtered.map { pagingData: PagingData<CollectionsFeedItem.WallpaperItem> ->
                    val wide: PagingData<CollectionsFeedItem> = pagingData.map { it }
                    if (filters.showSep && category == CollectionCategory.FAVORITES) {
                        wide.insertSeparators { before, after ->
                            if (after == null) return@insertSeparators null
                            val afterDate =
                                (after as? CollectionsFeedItem.WallpaperItem)?.localDate
                                    ?: return@insertSeparators null
                            val beforeDate =
                                (before as? CollectionsFeedItem.WallpaperItem)?.localDate
                            if (beforeDate != afterDate) CollectionsFeedItem.DateHeader(afterDate)
                            else null
                        }
                    } else {
                        wide
                    }
                }
            }
        }.cachedIn(viewModelScope)

    val uiState = combine(
        localUiState,
        selectedCategoryFlow,
        filtersFlow,
        appPreferencesRepository.appPreferencesFlow,
        favoritesRepository.observeAll(),
        viewedRepository.observeAll(),
        lightDarkRepository.observeAll(),
    ) { local: CollectionsUiStatePartial,
        selectedCategory: CollectionCategory,
        filters: FeedFilters,
        appPreferences: AppPreferences,
        favorites: List<FavoriteEntity>,
        viewedList: List<ViewedEntity>,
        lightDarkList: List<LightDarkEntity>,
        ->
        local.merge(
            CollectionsUiState(
                blurSketchy = appPreferences.blurSketchy,
                blurNsfw = appPreferences.blurNsfw,
                layoutPreferences = appPreferences.lookAndFeelPreferences.layoutPreferences,
                favorites = favorites.map(FavoriteEntity::toFavorite).toImmutableList(),
                viewedList = viewedList.map(ViewedEntity::toViewed).toImmutableList(),
                viewedWallpapersLook = appPreferences.viewedWallpapersPreferences.look,
                lightDarkList = lightDarkList.map(LightDarkEntity::toLightDark).toImmutableList(),
                prevMainWallhavenSearch = appPreferences.mainWallhavenSearch,
                prevMainRedditSearch = appPreferences.mainRedditSearch,
                selectedCategory = selectedCategory,
                selectedSourceFilter = filters.sourceFilter,
                selectedPurityFilter = filters.purityFilter,
                selectedAppearanceFilter = filters.appearanceFilter,
                selectedDateFilter = filters.dateFilter,
                showDateSeparators = filters.showSep,
                availableDates = favorites.mapTo(HashSet()) { e ->
                    e.favoritedOn.toLocalDateTime(TimeZone.UTC).date
                }.toImmutableSet(),
                telegramIsConfigured = appPreferences.telegramPreferences.enabled &&
                    appPreferences.telegramPreferences.isConfigured,
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CollectionsUiState(),
    )

    fun setSelectedWallpaper(wallpaper: Wallpaper) = localUiState.update {
        it.copy(selectedWallpaper = partial(wallpaper))
    }

    fun toggleFavorite(wallpaper: Wallpaper) = viewModelScope.launch {
        favoritesRepository.toggleFavorite(
            sourceId = wallpaper.id,
            source = wallpaper.source,
        )
    }

    fun changeCategory(category: CollectionCategory) = selectedCategoryFlow.update { category }

    fun setSourceFilter(source: Source?) = selectedSourceFilterFlow.update { source }

    fun setPurityFilter(purityFilter: PurityFilter) = selectedPurityFilterFlow.update { purityFilter }

    fun setAppearanceFilter(filter: AppearanceFilter) = selectedAppearanceFilterFlow.update { filter }

    fun setDateFilter(date: LocalDate?) = selectedDateFilterFlow.update { date }

    fun setShowDateSeparators(show: Boolean) = viewModelScope.launch {
        appPreferencesRepository.updateCollectionsShowDateSeparators(show)
    }

    fun setQuickActionsWallpaper(wallpaper: Wallpaper?) = localUiState.update {
        it.copy(quickActionsWallpaper = partial(wallpaper))
    }
}

@Stable
@Partialize
data class CollectionsUiState(
    val blurSketchy: Boolean = false,
    val blurNsfw: Boolean = false,
    val selectedWallpaper: Wallpaper? = null,
    val layoutPreferences: LayoutPreferences = LayoutPreferences(),
    val favorites: ImmutableList<Favorite> = persistentListOf(),
    val viewedList: ImmutableList<Viewed> = persistentListOf(),
    val viewedWallpapersLook: ViewedWallpapersLook = ViewedWallpapersLook.DIM_WITH_LABEL,
    val lightDarkList: ImmutableList<LightDark> = persistentListOf(),
    val prevMainWallhavenSearch: WallhavenSearch? = null,
    val prevMainRedditSearch: RedditSearch? = null,
    val selectedCategory: CollectionCategory = CollectionCategory.FAVORITES,
    val selectedSourceFilter: Source? = null,
    val selectedPurityFilter: PurityFilter = PurityFilter.ALL,
    val selectedAppearanceFilter: AppearanceFilter = AppearanceFilter.ALL,
    val selectedDateFilter: LocalDate? = null,
    val showDateSeparators: Boolean = false,
    val availableDates: ImmutableSet<LocalDate> = persistentSetOf(),
    val quickActionsWallpaper: Wallpaper? = null,
    val telegramIsConfigured: Boolean = false,
)
