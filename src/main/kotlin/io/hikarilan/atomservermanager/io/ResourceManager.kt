package io.hikarilan.atomservermanager.io

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.res.ResourceLoader
import java.io.InputStream

@PublishedApi
@OptIn(ExperimentalComposeUiApi::class)
internal fun openResourceOrNull(
    resourcePath: String,
): InputStream? {
    return try {
        ResourceLoader.Default.load(resourcePath)
    } catch (e: IllegalArgumentException) {
        null
    }
}