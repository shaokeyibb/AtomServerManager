package io.hikarilan.atomservermanager.utils

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ScaleFactor

class KnownScaleWrapperContentScale(
    private val scale: Float,
    private val baseContentScale: ContentScale
) : ContentScale {
    override fun computeScaleFactor(srcSize: Size, dstSize: Size): ScaleFactor =
        baseContentScale.computeScaleFactor(Size(srcSize.width * scale, srcSize.height), dstSize)
            .let { it.copy(it.scaleX * scale, it.scaleY) }
}