package com.ammar.wallflow.ui.common

import android.content.res.Configuration
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ammar.wallflow.R
import com.ammar.wallflow.ui.theme.WallFlowTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> SegmentedButtons(
    modifier: Modifier = Modifier,
    options: List<SegmentedButtonOption<T>>,
    mode: SegmentedButtonsMode = SegmentedButtonsMode.MULTI_SELECT,
    value: Set<T>,
    enabled: Boolean = true,
    onChange: (values: Set<T>) -> Unit = {},
) {
    ButtonGroup(
        modifier = modifier,
    ) {
        options.forEach { option ->
            val isChecked = option.value in value
            val currentEnabled = enabled && option.enabled
            ToggleButton(
                checked = isChecked,
                onCheckedChange = { checked ->
                    onChange(
                        if (mode == SegmentedButtonsMode.SINGLE_SELECT) {
                            setOf(option.value)
                        } else {
                            if (checked) {
                                value + option.value
                            } else {
                                value.filter { it != option.value }.toSet()
                            }
                        },
                    )
                },
                enabled = currentEnabled,
                modifier = Modifier.weight(1f),
            ) {
                if (isChecked) {
                    Icon(
                        modifier = Modifier.size(18.dp),
                        imageVector = Icons.Outlined.Check,
                        contentDescription = stringResource(R.string.selected),
                    )
                    Spacer(modifier = Modifier.requiredWidth(4.dp))
                }
                if (!isChecked || option.text == null) {
                    option.icon?.run {
                        Icon(
                            modifier = Modifier.size(18.dp),
                            painter = this(),
                            contentDescription = "",
                        )
                    }
                }
                if (!isChecked && option.icon != null && option.text != null) {
                    Spacer(modifier = Modifier.requiredWidth(4.dp))
                }
                option.text?.run {
                    Text(
                        text = this,
                        overflow = TextOverflow.Clip,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

enum class SegmentedButtonsMode {
    MULTI_SELECT,
    SINGLE_SELECT,
}

data class SegmentedButtonOption<T>(
    val value: T,
    val enabled: Boolean = true,
    val icon: (@Composable () -> Painter)? = null,
    val text: String? = null,
)

private val tempToggleOptions: List<SegmentedButtonOption<String>> = listOf(
    SegmentedButtonOption(
        "First",
        text = "First",
    ),
    SegmentedButtonOption(
        "Second",
        icon = { rememberVectorPainter(Icons.AutoMirrored.Rounded.List) },
        text = "Second",
    ),
    SegmentedButtonOption(
        "Third",
        icon = { rememberVectorPainter(Icons.AutoMirrored.Rounded.List) },
    ),
    SegmentedButtonOption(
        "Fourth",
        false,
        icon = { rememberVectorPainter(Icons.AutoMirrored.Rounded.List) },
    ),
)

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewButtonToggle() {
    var value by remember { mutableStateOf(setOf("First", "Third")) }
    WallFlowTheme {
        Surface {
            SegmentedButtons(
                options = tempToggleOptions,
                value = value,
                onChange = { value = it },
            )
        }
    }
}
