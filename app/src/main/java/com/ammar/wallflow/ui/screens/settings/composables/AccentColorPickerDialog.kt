package com.ammar.wallflow.ui.screens.settings.composables

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ammar.wallflow.R

// Corner percent: 50 = circle, 28 ≈ squircle (Cookie4Sided approximation)
private const val CORNER_CIRCLE = 50
private const val CORNER_SQUIRCLE = 28

private data class ColorSwatch(val label: String, val color: Int)

private val COLOR_SWATCHES = listOf(
    ColorSwatch("Red",          0xFFE53935.toInt()),
    ColorSwatch("Pink",         0xFFE91E63.toInt()),
    ColorSwatch("Purple",       0xFF6650A4.toInt()),
    ColorSwatch("Deep Purple",  0xFF512DA8.toInt()),
    ColorSwatch("Indigo",       0xFF3949AB.toInt()),
    ColorSwatch("Blue",         0xFF1E88E5.toInt()),
    ColorSwatch("Cyan",         0xFF00ACC1.toInt()),
    ColorSwatch("Teal",         0xFF00897B.toInt()),
    ColorSwatch("Green",        0xFF43A047.toInt()),
    ColorSwatch("Lime",         0xFF7CB342.toInt()),
    ColorSwatch("Amber",        0xFFFFB300.toInt()),
    ColorSwatch("Orange",       0xFFFB8C00.toInt()),
    ColorSwatch("Deep Orange",  0xFFF4511E.toInt()),
    ColorSwatch("Brown",        0xFF6D4C41.toInt()),
    ColorSwatch("Blue Grey",    0xFF546E7A.toInt()),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AccentColorPickerDialog(
    modifier: Modifier = Modifier,
    selectedColor: Int? = null,
    onColorSelected: (Int?) -> Unit = {},
    onDismissRequest: () -> Unit = {},
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.accent_color)) },
        text = {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Default swatch (clear / use dynamic)
                val isDefault = selectedColor == null
                val defaultCorner by animateIntAsState(
                    targetValue = if (isDefault) CORNER_SQUIRCLE else CORNER_CIRCLE,
                    label = "defaultSwatchCorner",
                )
                val defaultShape = RoundedCornerShape(defaultCorner)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(defaultShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            width = if (isDefault) 3.dp else 1.dp,
                            color = if (isDefault) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline,
                            shape = defaultShape,
                        )
                        .clickable { onColorSelected(null) }
                        .padding(10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.default_color).take(1),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                COLOR_SWATCHES.forEach { swatch ->
                    val isSelected = selectedColor == swatch.color
                    val swatchCorner by animateIntAsState(
                        targetValue = if (isSelected) CORNER_SQUIRCLE else CORNER_CIRCLE,
                        label = "swatchCorner_${swatch.label}",
                    )
                    val swatchShape = RoundedCornerShape(swatchCorner)
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(swatchShape)
                            .background(Color(swatch.color))
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.outline
                                else Color.Transparent,
                                shape = swatchShape,
                            )
                            .clickable { onColorSelected(swatch.color) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isSelected) {
                            Icon(
                                modifier = Modifier.size(20.dp),
                                painter = painterResource(R.drawable.baseline_check_24),
                                contentDescription = swatch.label,
                                tint = Color.White,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(android.R.string.ok))
            }
        },
    )
}
