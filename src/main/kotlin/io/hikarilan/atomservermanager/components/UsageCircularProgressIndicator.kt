package io.hikarilan.atomservermanager.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

@Composable
fun UsageCircularProgressIndicator(
    modifier: Modifier = Modifier,
    progress: Float,
    progressIndicatorSize: Dp,
    color: Color = MaterialTheme.colors.primary,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.requiredSize(progressIndicatorSize),
        contentAlignment = Alignment.Center
    ) {

        val animatedProgress = animateFloatAsState(
            targetValue = progress,
            animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
        ).value

        CircularProgressIndicator(
            color = color,
            progress = animatedProgress,
            modifier = Modifier.matchParentSize().requiredSize(progressIndicatorSize)
        )
        content()
    }
}
