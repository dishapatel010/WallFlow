package com.ammar.wallflow.ui.screens.settings.detailcontents

import android.content.res.Configuration
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ammar.wallflow.R
import com.ammar.wallflow.data.preferences.TelegramPreferences
import com.ammar.wallflow.ui.screens.settings.composables.SettingsDetailListItem
import com.ammar.wallflow.ui.theme.WallFlowTheme

@Composable
internal fun TelegramContent(
    modifier: Modifier = Modifier,
    telegramPreferences: TelegramPreferences = TelegramPreferences(),
    isExpanded: Boolean = false,
    onEnabledChange: (Boolean) -> Unit = {},
    onBotTokenChange: (String) -> Unit = {},
    onChatIdChange: (String) -> Unit = {},
    onMessageThreadIdChange: (String) -> Unit = {},
    onPostAfterDownloadChange: (Boolean) -> Unit = {},
    onIncludeFileNameChange: (Boolean) -> Unit = {},
    onIncludeDateChange: (Boolean) -> Unit = {},
    onIncludeTagsChange: (Boolean) -> Unit = {},
    onIncludeSourceUrlChange: (Boolean) -> Unit = {},
    onSilentNotificationChange: (Boolean) -> Unit = {},
    onDisableWebPagePreviewChange: (Boolean) -> Unit = {},
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
    ) {
        // ── Connection ──────────────────────────────────────────────────────
        item {
            SettingsDetailListItem(
                modifier = Modifier.padding(horizontal = 16.dp),
                isExpanded = isExpanded,
                isFirst = true,
                isLast = false,
                headlineContent = {
                    Text(text = stringResource(R.string.telegram_enable_posting))
                },
                supportingContent = {
                    Text(text = stringResource(R.string.telegram_enable_posting_desc))
                },
                trailingContent = {
                    Switch(
                        checked = telegramPreferences.enabled,
                        onCheckedChange = onEnabledChange,
                    )
                },
            )
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                value = telegramPreferences.botToken,
                onValueChange = onBotTokenChange,
                label = { Text(stringResource(R.string.telegram_bot_token)) },
                supportingText = {
                    Text(stringResource(R.string.telegram_bot_token_desc))
                },
                singleLine = true,
            )
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                value = telegramPreferences.chatId,
                onValueChange = onChatIdChange,
                label = { Text(stringResource(R.string.telegram_chat_id)) },
                supportingText = {
                    Text(stringResource(R.string.telegram_chat_id_desc))
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                value = telegramPreferences.messageThreadId,
                onValueChange = onMessageThreadIdChange,
                label = { Text(stringResource(R.string.telegram_message_thread_id)) },
                supportingText = {
                    Text(stringResource(R.string.telegram_message_thread_id_desc))
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        }
        item {
            SettingsDetailListItem(
                modifier = Modifier.padding(horizontal = 16.dp),
                isExpanded = isExpanded,
                isFirst = false,
                isLast = true,
                headlineContent = {
                    Text(text = stringResource(R.string.telegram_post_after_download))
                },
                supportingContent = {
                    Text(text = stringResource(R.string.telegram_post_after_download_desc))
                },
                trailingContent = {
                    Switch(
                        checked = telegramPreferences.postAfterDownload,
                        onCheckedChange = onPostAfterDownloadChange,
                    )
                },
            )
        }

        // ── Caption content ─────────────────────────────────────────────────
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                text = stringResource(R.string.telegram_caption_section),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        item {
            SettingsDetailListItem(
                modifier = Modifier.padding(horizontal = 16.dp),
                isExpanded = isExpanded,
                isFirst = true,
                isLast = false,
                headlineContent = {
                    Text(text = stringResource(R.string.telegram_include_file_name))
                },
                supportingContent = {
                    Text(text = stringResource(R.string.telegram_include_file_name_desc))
                },
                trailingContent = {
                    Switch(
                        checked = telegramPreferences.includeFileName,
                        onCheckedChange = onIncludeFileNameChange,
                    )
                },
            )
        }
        item {
            SettingsDetailListItem(
                modifier = Modifier.padding(horizontal = 16.dp),
                isExpanded = isExpanded,
                isFirst = false,
                isLast = false,
                headlineContent = {
                    Text(text = stringResource(R.string.telegram_include_date))
                },
                supportingContent = {
                    Text(text = stringResource(R.string.telegram_include_date_desc))
                },
                trailingContent = {
                    Switch(
                        checked = telegramPreferences.includeDate,
                        onCheckedChange = onIncludeDateChange,
                    )
                },
            )
        }
        item {
            SettingsDetailListItem(
                modifier = Modifier.padding(horizontal = 16.dp),
                isExpanded = isExpanded,
                isFirst = false,
                isLast = false,
                headlineContent = {
                    Text(text = stringResource(R.string.telegram_include_tags))
                },
                supportingContent = {
                    Text(text = stringResource(R.string.telegram_include_tags_desc))
                },
                trailingContent = {
                    Switch(
                        checked = telegramPreferences.includeTags,
                        onCheckedChange = onIncludeTagsChange,
                    )
                },
            )
        }
        item {
            SettingsDetailListItem(
                modifier = Modifier.padding(horizontal = 16.dp),
                isExpanded = isExpanded,
                isFirst = false,
                isLast = true,
                headlineContent = {
                    Text(text = stringResource(R.string.telegram_include_source_url))
                },
                supportingContent = {
                    Text(text = stringResource(R.string.telegram_include_source_url_desc))
                },
                trailingContent = {
                    Switch(
                        checked = telegramPreferences.includeSourceUrl,
                        onCheckedChange = onIncludeSourceUrlChange,
                    )
                },
            )
        }

        // ── Delivery options ─────────────────────────────────────────────────
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                text = stringResource(R.string.telegram_delivery_section),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        item {
            SettingsDetailListItem(
                modifier = Modifier.padding(horizontal = 16.dp),
                isExpanded = isExpanded,
                isFirst = true,
                isLast = false,
                headlineContent = {
                    Text(text = stringResource(R.string.telegram_silent_notification))
                },
                supportingContent = {
                    Text(text = stringResource(R.string.telegram_silent_notification_desc))
                },
                trailingContent = {
                    Switch(
                        checked = telegramPreferences.silentNotification,
                        onCheckedChange = onSilentNotificationChange,
                    )
                },
            )
        }
        item {
            SettingsDetailListItem(
                modifier = Modifier.padding(horizontal = 16.dp),
                isExpanded = isExpanded,
                isFirst = false,
                isLast = true,
                headlineContent = {
                    Text(text = stringResource(R.string.telegram_disable_web_page_preview))
                },
                supportingContent = {
                    Text(text = stringResource(R.string.telegram_disable_web_page_preview_desc))
                },
                trailingContent = {
                    Switch(
                        checked = telegramPreferences.disableWebPagePreview,
                        onCheckedChange = onDisableWebPagePreviewChange,
                    )
                },
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewTelegramContent() {
    WallFlowTheme {
        Surface {
            TelegramContent()
        }
    }
}
