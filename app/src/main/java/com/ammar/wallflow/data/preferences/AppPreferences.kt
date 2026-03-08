@file:UseSerializers(
    DateTimePeriodSerializer::class,
    ConstraintsSerializer::class,
    UUIDSerializer::class,
    UriSerializer::class,
)

package com.ammar.wallflow.data.preferences

import android.net.Uri
import androidx.annotation.IntRange
import androidx.work.Constraints
import androidx.work.NetworkType
import com.ammar.wallflow.model.OnlineSource
import com.ammar.wallflow.model.WallpaperTarget
import com.ammar.wallflow.model.search.RedditSearch
import com.ammar.wallflow.model.search.RedditSubredditFilter
import com.ammar.wallflow.model.search.WallhavenFilters
import com.ammar.wallflow.model.search.WallhavenSearch
import com.ammar.wallflow.model.search.WallhavenSorting
import com.ammar.wallflow.model.search.WallhavenTopRange
import com.ammar.wallflow.model.serializers.ConstraintsSerializer
import com.ammar.wallflow.model.serializers.DateTimePeriodSerializer
import com.ammar.wallflow.model.serializers.UUIDSerializer
import com.ammar.wallflow.model.serializers.UriSerializer
import com.ammar.wallflow.ui.screens.local.LocalSort
import com.ammar.wallflow.utils.ExifWriteType
import com.ammar.wallflow.workers.AutoWallpaperWorker.Companion.SourceChoice
import java.util.UUID
import kotlinx.datetime.DateTimePeriod
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class AppPreferences(
    val version: Int? = CURRENT_VERSION,
    val wallhavenApiKey: String = "",
    val homeWallhavenSearch: WallhavenSearch = WallhavenSearch(
        filters = WallhavenFilters(
            sorting = WallhavenSorting.TOPLIST,
            topRange = WallhavenTopRange.ONE_DAY,
        ),
    ),
    val homeRedditSearch: RedditSearch? = null,
    val homeSources: Map<OnlineSource, Boolean> = mapOf(OnlineSource.WALLHAVEN to true),
    val redditSubredditFilter: RedditSubredditFilter = RedditSubredditFilter(),
    val blurSketchy: Boolean = false,
    val blurNsfw: Boolean = false,
    val writeTagsToExif: Boolean = false,
    val tagsExifWriteType: ExifWriteType = ExifWriteType.APPEND,
    val objectDetectionPreferences: ObjectDetectionPreferences = ObjectDetectionPreferences(),
    val autoWallpaperPreferences: AutoWallpaperPreferences = AutoWallpaperPreferences(),
    val lookAndFeelPreferences: LookAndFeelPreferences = LookAndFeelPreferences(),
    val changeWallpaperTileAdded: Boolean = false,
    val localWallpapersPreferences: LocalWallpapersPreferences = LocalWallpapersPreferences(),
    val mainWallhavenSearch: WallhavenSearch? = null,
    val mainRedditSearch: RedditSearch? = null,
    val viewedWallpapersPreferences: ViewedWallpapersPreferences = ViewedWallpapersPreferences(),
    val downloadLocation: Uri? = null,
    val acraEnabled: Boolean = true,
    val telegramPreferences: TelegramPreferences = TelegramPreferences(),
) {
    companion object {
        const val CURRENT_VERSION = 2
    }
}

@Serializable
data class TelegramPreferences(
    val enabled: Boolean = false,
    val botToken: String = "",
    val chatId: String = "",
    val messageThreadId: String = "",
    val postAfterDownload: Boolean = false,
    val includeFileName: Boolean = true,
    val includeDate: Boolean = true,
    val includeTags: Boolean = true,
    val includeSourceUrl: Boolean = true,
    val silentNotification: Boolean = false,
    val disableWebPagePreview: Boolean = false,
) {
    val isConfigured: Boolean get() = botToken.isNotBlank() && chatId.isNotBlank()
}

enum class ObjectDetectionDelegate {
    NONE,
    NNAPI,
    GPU,
}

@Serializable
data class ObjectDetectionPreferences(
    val enabled: Boolean = false,
    val delegate: ObjectDetectionDelegate = ObjectDetectionDelegate.GPU,
    val modelId: Long = 0,
)

internal val defaultAutoWallpaperFreq = DateTimePeriod(hours = 4)
internal val defaultAutoWallpaperConstraints = Constraints.Builder().apply {
    setRequiredNetworkType(NetworkType.CONNECTED)
}.build()

@Serializable
data class AutoWallpaperPreferences(
    val enabled: Boolean = false,
    val savedSearchEnabled: Boolean = false,
    val lsSavedSearchEnabled: Boolean = false,
    val favoritesEnabled: Boolean = false,
    val lsFavoritesEnabled: Boolean = false,
    val localEnabled: Boolean = false,
    val lsLocalEnabled: Boolean = false,
    val savedSearchIds: Set<Long> = emptySet(),
    val lsSavedSearchIds: Set<Long> = emptySet(),
    val localDirs: Set<Uri> = emptySet(),
    val lsLocalDirs: Set<Uri> = emptySet(),
    val useObjectDetection: Boolean = true,
    val useSameFreq: Boolean = true,
    val frequency: DateTimePeriod = defaultAutoWallpaperFreq,
    val lsFrequency: DateTimePeriod = defaultAutoWallpaperFreq,
    val constraints: Constraints = defaultAutoWallpaperConstraints,
    val showNotification: Boolean = false,
    val workRequestId: UUID? = null,
    val lsWorkRequestId: UUID? = null,
    val targets: Set<WallpaperTarget> = setOf(WallpaperTarget.HOME, WallpaperTarget.LOCKSCREEN),
    val markFavorite: Boolean = false,
    val download: Boolean = false,
    val setDifferentWallpapers: Boolean = false,
    val crop: Boolean = true,
    val lightDarkEnabled: Boolean = false,
    val lsLightDarkEnabled: Boolean = false,
    val useDarkWithExtraDim: Boolean = false,
    val lsUseDarkWithExtraDim: Boolean = false,
    val backoffUpdated: Boolean = false,
    val prevHomeSource: SourceChoice? = null,
    val prevLockScreenSource: SourceChoice? = null,
) {
    val anyHomeScreenSourceEnabled = lightDarkEnabled || (
        savedSearchEnabled &&
            savedSearchIds.isNotEmpty() &&
            savedSearchIds.all { it > 0 }
        ) ||
        favoritesEnabled ||
        localEnabled
    val anyLockScreenSourceEnabled = lsLightDarkEnabled || (
        lsSavedSearchEnabled &&
            lsSavedSearchIds.isNotEmpty() &&
            lsSavedSearchIds.all { it > 0 }
        ) ||
        lsFavoritesEnabled ||
        lsLocalEnabled
    val anySourceEnabled = anyHomeScreenSourceEnabled || anyLockScreenSourceEnabled
}

enum class Theme {
    SYSTEM,
    LIGHT,
    DARK,
}

@Serializable
data class LookAndFeelPreferences(
    val theme: Theme = Theme.SYSTEM,
    val layoutPreferences: LayoutPreferences = LayoutPreferences(),
    val showLocalTab: Boolean = true,
    val accentColor: Int? = null,
)

@Serializable
data class ViewedWallpapersPreferences(
    val enabled: Boolean = false,
    val look: ViewedWallpapersLook = ViewedWallpapersLook.DIM_WITH_LABEL,
)

enum class ViewedWallpapersLook {
    NONE,
    DIM,
    DIM_WITH_LABEL,
    DIM_WITH_ICON,
    LABEL,
    ICON,
}

enum class GridType {
    STAGGERED,
    FIXED_SIZE,
}

enum class GridColType {
    ADAPTIVE,
    FIXED,
}

const val MIN_GRID_COLS = 1L
const val MAX_GRID_COLS = 5L
const val MIN_GRID_COL_WIDTH_PCT = 10L
const val MAX_GRID_COL_WIDTH_PCT = 50L
const val MIN_GRID_ITEM_SPACING_DP = 0L
const val MAX_GRID_ITEM_SPACING_DP = 16L

@Serializable
data class LayoutPreferences(
    val gridType: GridType = GridType.STAGGERED,
    val gridColType: GridColType = GridColType.ADAPTIVE,
    @IntRange(MIN_GRID_COLS, MAX_GRID_COLS) val gridColCount: Int = 2,
    @IntRange(MIN_GRID_COL_WIDTH_PCT, MAX_GRID_COL_WIDTH_PCT) val gridColMinWidthPct: Int = 40,
    val roundedCorners: Boolean = true,
    @IntRange(MIN_GRID_ITEM_SPACING_DP, MAX_GRID_ITEM_SPACING_DP) val gridItemSpacingDp: Int = 8,
    val showCarousel: Boolean = true,
    val showCollectionsDateSeparators: Boolean = false,
)

@Serializable
data class LocalWallpapersPreferences(
    val sort: LocalSort = LocalSort.NO_SORT,
    val directories: Set<Uri>? = null,
)
