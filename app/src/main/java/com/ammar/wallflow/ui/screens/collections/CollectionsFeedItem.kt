package com.ammar.wallflow.ui.screens.collections

import com.ammar.wallflow.model.Wallpaper
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

sealed class CollectionsFeedItem {
    /**
     * A wallpaper item carrying the timestamp at which it was favorited. For non-favorites
     * categories (e.g. LIGHT_DARK) [favoritedOn] is set to epoch zero and should be ignored.
     */
    data class WallpaperItem(
        val wallpaper: Wallpaper,
        val favoritedOn: Instant,
    ) : CollectionsFeedItem() {
        /** Local calendar date derived from [favoritedOn]. */
        val localDate: LocalDate
            get() = favoritedOn.toLocalDateTime(TimeZone.currentSystemDefault()).date
    }

    /** A full-width date separator injected between wallpaper groups. */
    data class DateHeader(val date: LocalDate) : CollectionsFeedItem()
}
