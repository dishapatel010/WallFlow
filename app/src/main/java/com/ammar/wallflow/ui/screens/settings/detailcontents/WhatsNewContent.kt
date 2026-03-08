package com.ammar.wallflow.ui.screens.settings.detailcontents

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.ammar.wallflow.R
import com.ammar.wallflow.ui.screens.settings.composables.SettingsDetailListItem
import com.ammar.wallflow.ui.theme.WallFlowTheme

private data class ChangelogEntry(val titleRes: Int, val bodyRes: Int)

private val entries = listOf(
    ChangelogEntry(R.string.whats_new_telegram_title, R.string.whats_new_telegram_body),
    ChangelogEntry(R.string.whats_new_gallery_title, R.string.whats_new_gallery_body),
    ChangelogEntry(R.string.whats_new_fav_gallery_title, R.string.whats_new_fav_gallery_body),
    ChangelogEntry(R.string.whats_new_collections_title, R.string.whats_new_collections_body),
    ChangelogEntry(R.string.whats_new_quick_actions_title, R.string.whats_new_quick_actions_body),
    ChangelogEntry(R.string.whats_new_accent_title, R.string.whats_new_accent_body),
    ChangelogEntry(R.string.whats_new_subreddits_title, R.string.whats_new_subreddits_body),
    ChangelogEntry(R.string.whats_new_m3exp_title, R.string.whats_new_m3exp_body),
)

@Composable
internal fun WhatsNewContent(
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        entries.forEachIndexed { index, entry ->
            item(key = entry.titleRes) {
                SettingsDetailListItem(
                    isExpanded = isExpanded,
                    isFirst = index == 0,
                    isLast = index == entries.lastIndex,
                    headlineContent = {
                        Text(text = stringResource(entry.titleRes))
                    },
                    supportingContent = {
                        Text(text = stringResource(entry.bodyRes))
                    },
                )
            }
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewWhatsNewContent() {
    WallFlowTheme {
        Surface {
            WhatsNewContent()
        }
    }
}
