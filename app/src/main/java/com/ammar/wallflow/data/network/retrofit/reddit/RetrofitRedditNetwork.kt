package com.ammar.wallflow.data.network.retrofit.reddit

import com.ammar.wallflow.data.network.RedditNetworkDataSource
import com.ammar.wallflow.extensions.trimAll
import com.ammar.wallflow.model.search.RedditSearch

class RetrofitRedditNetwork(
    private val redditNetworkApi: RedditNetworkApi,
) : RedditNetworkDataSource {
    override suspend fun search(
        search: RedditSearch,
        after: String?,
    ) = with(search.filters) {
        require(subreddits.isNotEmpty()) {
            "At least one subreddit must be configured for Reddit search."
        }
        redditNetworkApi.search(
            query = "self:no ${search.query}".trimAll(),
            subreddit = subreddits.joinToString("+"),
            includeNsfw = if (includeNsfw) "on" else "off",
            sort = sort.value,
            timeRange = timeRange.value,
            after = after,
        )
    }
}
