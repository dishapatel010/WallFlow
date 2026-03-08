package com.ammar.wallflow.model.search

import kotlinx.serialization.Serializable

@Serializable
data class RedditSubredditFilter(
    val includedSubreddits: Set<String> = emptySet(),
    val excludedSubreddits: Set<String> = emptySet(),
) {
    // Only excludes are used — includedSubreddits is kept for serialisation compat but ignored.
    val isActive: Boolean
        get() = excludedSubreddits.isNotEmpty()

    /**
     * Applies exclusions on top of [configuredSubreddits] and returns the effective subreddit set.
     */
    fun apply(configuredSubreddits: Set<String>): Set<String> {
        return configuredSubreddits - excludedSubreddits
    }
}
