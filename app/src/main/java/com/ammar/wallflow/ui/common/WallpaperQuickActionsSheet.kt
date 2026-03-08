package com.ammar.wallflow.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ammar.wallflow.R
import com.ammar.wallflow.extensions.aspectRatio
import com.ammar.wallflow.model.Wallpaper
import com.ammar.wallflow.model.wallhaven.WallhavenWallpaper

/**
 * Pinterest-style quick-actions sheet that slides up on long-press of a wallpaper card.
 *
 * Available actions:
 *  • Favorite / Unfavorite
 *  • Apply as wallpaper
 *  • Download
 *  • Share
 *  • Post to Telegram (shown only when Telegram is configured)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperQuickActionsSheet(
    wallpaper: Wallpaper,
    isFavorite: Boolean = false,
    showApplyWallpaper: Boolean = true,
    showDownload: Boolean = true,
    showShare: Boolean = true,
    showTelegram: Boolean = false,
    onDismiss: () -> Unit = {},
    onFavoriteClick: () -> Unit = {},
    onApplyWallpaperClick: () -> Unit = {},
    onDownloadClick: () -> Unit = {},
    onShareLinkClick: () -> Unit = {},
    onShareImageClick: () -> Unit = {},
    onPostToTelegramClick: () -> Unit = {},
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // ── Thumbnail ───────────────────────────────────────────────────
            val thumbAspect = runCatching { wallpaper.resolution.aspectRatio }
                .getOrDefault(1f)
                .coerceIn(0.4f, 2.5f)
            AsyncImage(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .aspectRatio(thumbAspect)
                    .heightIn(max = 160.dp)
                    .clip(RoundedCornerShape(12.dp)),
                model = ImageRequest.Builder(context)
                    .data(wallpaper.thumbData ?: wallpaper.data)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )

            Spacer(Modifier.height(12.dp))

            // ── Wallpaper id label ──────────────────────────────────────────
            Text(
                text = wallpaper.id,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            // ── Action row ──────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Favorite
                QuickActionItem(
                    label = stringResource(if (isFavorite) R.string.unfavorite else R.string.favorite),
                    icon = {
                        Icon(
                            painter = painterResource(
                                if (isFavorite) R.drawable.baseline_favorite_24
                                else R.drawable.outline_favorite_border_24,
                            ),
                            contentDescription = null,
                            tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    onClick = {
                        onFavoriteClick()
                        onDismiss()
                    },
                )

                // Apply wallpaper
                if (showApplyWallpaper) {
                    QuickActionItem(
                        label = stringResource(R.string.apply_wallpaper),
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.outline_wallpaper_24),
                                contentDescription = null,
                            )
                        },
                        onClick = {
                            onApplyWallpaperClick()
                            onDismiss()
                        },
                    )
                }

                // Download
                if (showDownload) {
                    QuickActionItem(
                        label = stringResource(R.string.download),
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.outline_file_download_24),
                                contentDescription = null,
                            )
                        },
                        onClick = {
                            onDownloadClick()
                            onDismiss()
                        },
                    )
                }

                // Share link
                if (showShare) {
                    QuickActionItem(
                        label = stringResource(R.string.share),
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                            )
                        },
                        onClick = {
                            onShareLinkClick()
                            onDismiss()
                        },
                    )
                }

                // Post to Telegram
                if (showTelegram) {
                    QuickActionItem(
                        label = stringResource(R.string.post_to_telegram),
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.baseline_send_24),
                                contentDescription = null,
                            )
                        },
                        onClick = {
                            onPostToTelegramClick()
                            onDismiss()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionItem(
    label: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        FilledTonalIconButton(
            onClick = onClick,
            modifier = Modifier.size(52.dp),
        ) {
            icon()
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
