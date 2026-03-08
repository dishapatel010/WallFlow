package com.ammar.wallflow.ui.screens.settings.detailcontents

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ammar.wallflow.R
import com.ammar.wallflow.ui.screens.settings.SettingsExtraType
import com.ammar.wallflow.ui.screens.settings.composables.SettingsDetailListItem
import com.ammar.wallflow.ui.theme.WallFlowTheme

import androidx.compose.foundation.lazy.LazyColumn

@Composable
fun LookAndFeelContent(
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false,
    selectedType: SettingsExtraType? = null,
    blurSketchy: Boolean = false,
    blurNsfw: Boolean = false,
    showLocalTab: Boolean = true,
    accentColor: Int? = null,
    onThemeClick: () -> Unit = {},
    onLayoutClick: () -> Unit = {},
    onAccentColorClick: () -> Unit = {},
    onBlurSketchyCheckChange: (checked: Boolean) -> Unit = {},
    onBlurNsfwCheckChange: (checked: Boolean) -> Unit = {},
    showCarousel: Boolean = true,
    onShowCarouselChange: (Boolean) -> Unit = {},
    onShowLocalTabChange: (Boolean) -> Unit = {},
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
    ) {
        item {
            SettingsDetailListItem(
                modifier = Modifier.clickable(onClick = onThemeClick),
                isExpanded = isExpanded,
                isFirst = true,
                headlineContent = { Text(text = stringResource(R.string.theme)) },
            )
        }
        item {
            SettingsDetailListItem(
                modifier = Modifier.clickable(onClick = onLayoutClick),
                isExpanded = isExpanded,
                selected = selectedType == SettingsExtraType.LAYOUT,
                headlineContent = { Text(text = stringResource(R.string.layout)) },
            )
        }
        item {
            SettingsDetailListItem(
                modifier = Modifier.clickable(onClick = onAccentColorClick),
                isExpanded = isExpanded,
                headlineContent = { Text(text = stringResource(R.string.accent_color)) },
                supportingContent = { Text(text = stringResource(R.string.accent_color_desc)) },
                trailingContent = {
                    val circleColor = if (accentColor != null) Color(accentColor) else MaterialTheme.colorScheme.primary
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(circleColor)
                            .border(
                                width = if (accentColor != null) 2.dp else 0.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = CircleShape,
                            ),
                    )
                },
            )
        }
        item {
            SettingsDetailListItem(
                modifier = Modifier.clickable { onBlurSketchyCheckChange(!blurSketchy) },
                isExpanded = isExpanded,
                headlineContent = { Text(text = stringResource(R.string.blur_sketchy_wallpapers)) },
                trailingContent = {
                    Switch(
                        modifier = Modifier.height(24.dp),
                        checked = blurSketchy,
                        onCheckedChange = onBlurSketchyCheckChange,
                    )
                },
            )
        }
        item {
            SettingsDetailListItem(
                modifier = Modifier.clickable { onBlurNsfwCheckChange(!blurNsfw) },
                isExpanded = isExpanded,
                headlineContent = { Text(text = stringResource(R.string.blur_nsfw_wallpapers)) },
                trailingContent = {
                    Switch(
                        modifier = Modifier.height(24.dp),
                        checked = blurNsfw,
                        onCheckedChange = onBlurNsfwCheckChange,
                    )
                },
            )
        }
        item {
            SettingsDetailListItem(
                modifier = Modifier.clickable { onShowCarouselChange(!showCarousel) },
                isExpanded = isExpanded,
                headlineContent = { Text(text = stringResource(R.string.show_carousel)) },
                supportingContent = { Text(text = stringResource(R.string.show_carousel_desc)) },
                trailingContent = {
                    Switch(
                        modifier = Modifier.height(24.dp),
                        checked = showCarousel,
                        onCheckedChange = onShowCarouselChange,
                    )
                },
            )
        }
        item {
            SettingsDetailListItem(
                modifier = Modifier.clickable { onShowLocalTabChange(!showLocalTab) },
                isExpanded = isExpanded,
                isLast = true,
                headlineContent = { Text(text = stringResource(R.string.show_local_tab)) },
                trailingContent = {
                    Switch(
                        modifier = Modifier.height(24.dp),
                        checked = showLocalTab,
                        onCheckedChange = onShowLocalTabChange,
                    )
                },
            )
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewLookAndFeelContent() {
    WallFlowTheme {
        Surface {
            LookAndFeelContent()
        }
    }
}
