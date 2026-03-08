package com.ammar.wallflow.ui.common

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProgressIndicator(
    modifier: Modifier = Modifier,
    progress: Float = -1F,
    strokeCap: StrokeCap = StrokeCap.Round,
    circular: Boolean = true,
) {
    if (progress <= -1F) {
        if (circular) {
            // M3 Expressive: LoadingIndicator replaces indeterminate CircularProgressIndicator
            LoadingIndicator(
                modifier = modifier.testTag("circular-progress"),
            )
        } else {
            LinearWavyProgressIndicator(
                modifier = modifier.testTag("linear-progress"),
            )
        }
    } else {
        if (circular) {
            // M3 Expressive: determinate LoadingIndicator
            LoadingIndicator(
                progress = { progress },
                modifier = modifier
                    .testTag("circular-progress")
                    .semantics { contentDescription = "Progress $progress" },
            )
        } else {
            LinearWavyProgressIndicator(
                progress = { progress },
                modifier = modifier
                    .testTag("linear-progress")
                    .semantics { contentDescription = "Progress $progress" },
            )
        }
    }
}
