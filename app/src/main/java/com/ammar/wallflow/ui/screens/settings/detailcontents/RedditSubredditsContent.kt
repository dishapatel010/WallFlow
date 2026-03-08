package com.ammar.wallflow.ui.screens.settings.detailcontents

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ammar.wallflow.R
import com.ammar.wallflow.ui.screens.settings.composables.SettingsDetailListItem
import com.ammar.wallflow.ui.theme.WallFlowTheme

@Composable
internal fun RedditSubredditsContent(
    modifier: Modifier = Modifier,
    subreddits: Set<String> = emptySet(),
    isExpanded: Boolean = false,
    onAddSubreddit: (String) -> Unit = {},
    onRemoveSubreddit: (String) -> Unit = {},
) {
    var newSubreddit by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
    ) {
        // Header with description
        item {
            SettingsDetailListItem(
                modifier = Modifier.padding(horizontal = 16.dp),
                isExpanded = isExpanded,
                isFirst = true,
                isLast = false,
                headlineContent = {
                    Text(text = stringResource(R.string.reddit_subreddits_title))
                },
                supportingContent = {
                    Text(text = stringResource(R.string.reddit_subreddits_desc))
                },
            )
        }

        // Add new subreddit field
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = newSubreddit,
                    onValueChange = { newSubreddit = it },
                    label = { Text(stringResource(R.string.add_subreddit)) },
                    placeholder = { Text("memes") },
                    supportingText = {
                        Text(stringResource(R.string.subreddit_name_only))
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                )
                IconButton(
                    onClick = {
                        if (newSubreddit.isNotBlank()) {
                            onAddSubreddit(newSubreddit.trim().lowercase())
                            newSubreddit = ""
                        }
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.add),
                    )
                }
            }
        }

        // Current subreddits list
        item {
            Spacer(modifier = Modifier.height(16.dp))
            if (subreddits.isNotEmpty()) {
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = stringResource(R.string.current_subreddits),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        items(
            items = subreddits.toList().sorted(),
            key = { it },
        ) { subreddit ->
            SubredditItem(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                subreddit = subreddit,
                onRemove = { onRemoveSubreddit(subreddit) },
            )
        }

        if (subreddits.isEmpty()) {
            item {
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 32.dp),
                    text = stringResource(R.string.no_subreddits_added),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
        }
    }
}

@Composable
private fun SubredditItem(
    subreddit: String,
    modifier: Modifier = Modifier,
    onRemove: () -> Unit = {},
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "r/$subreddit",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RedditSubredditsContentPreview() {
    WallFlowTheme {
        Surface {
            RedditSubredditsContent(
                subreddits = setOf("Memes", "wallpapers", "androidwallpapers"),
            )
        }
    }
}