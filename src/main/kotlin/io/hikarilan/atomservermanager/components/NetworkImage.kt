package io.hikarilan.atomservermanager.components

import androidx.compose.foundation.Image
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.unit.Density
import io.hikarilan.atomservermanager.io.Client
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request


@Composable
fun NetworkImage(
    url: String,
    isSvgImage: Boolean = url.endsWith(".svg"),
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
) {
    val isLoadedSuccessfully = remember { mutableStateOf(false) }

    val isError = remember { mutableStateOf(false) }

    val painter = remember { mutableStateOf<Painter?>(null) }

    LaunchedEffect("Load Network Image") {
        try {
            painter.value = withContext(Dispatchers.IO) {
                Client.okHttpClient.newCall(
                    Request.Builder()
                        .url(url)
                        .get()
                        .build()
                ).execute().body!!.byteStream().use {
                    if (isSvgImage) {
                        loadSvgPainter(it, Density(1.0f))
                    } else {
                        BitmapPainter(loadImageBitmap(it), filterQuality = FilterQuality.Low)
                    }
                }
            }
            isLoadedSuccessfully.value = true
        } catch (e: Throwable) {
            isError.value = true
        }
    }

    if (isLoadedSuccessfully.value && !isError.value)
        Image(
            painter = painter.value!!,
            modifier = modifier,
            contentDescription = contentDescription,
            alignment = alignment,
            contentScale = contentScale,
            alpha = alpha,
            colorFilter = colorFilter
        )
    else if (!isLoadedSuccessfully.value && !isError.value) {
        CircularProgressIndicator(modifier = modifier)
    } else if (!isLoadedSuccessfully.value && isError.value) {
        ErrorImage(modifier, contentDescription)
    } else {
        throw IllegalStateException("Unreachable state ${isLoadedSuccessfully.value} && ${isError.value}")
    }

}

@Composable
fun ErrorImage(
    modifier: Modifier = Modifier,
    contentDescription: String?,
) {
    Image(
        modifier = modifier,
        imageVector = Icons.Default.Close,
        contentDescription = contentDescription,
    )
}