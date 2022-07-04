package io.hikarilan.atomservermanager

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.gson.GsonBuilder
import org.jetbrains.skia.defaultLanguageTag
import java.io.File
import java.net.InetSocketAddress
import java.net.Proxy
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists

object Settings {

    private val path: Path = Paths.get("settings.json")

    private val jsonSettings = load()

    var locale by mutableStateOf(jsonSettings.locale)

    var backgroundImageType by mutableStateOf(jsonSettings.background.imageType)

    var backgroundImage: File? by mutableStateOf(jsonSettings.background.image?.let { File(it) })

    var opacity by mutableStateOf(jsonSettings.opacity)

    var proxy by mutableStateOf(jsonSettings.proxy?.let { Proxy(it.type, InetSocketAddress(it.host, it.port)) })

    var disableVerify by mutableStateOf(jsonSettings.disableVerify)

    object BackgroundImageType {
        const val IMAGE = 0
        const val TRANSPARENT = 2
    }

    private fun generateJsonSettings(): JsonSettings {
        return JsonSettings(
            locale = locale,
            background = JsonSettings.BackgroundSettings(
                imageType = backgroundImageType,
                image = backgroundImage?.absolutePath
            ),
            opacity = opacity,
            proxy = proxy?.let {
                JsonSettings.ProxySettings(
                    it.type(),
                    (it.address() as InetSocketAddress).hostString,
                    (it.address() as InetSocketAddress).port
                )
            },
            disableVerify = disableVerify
        )
    }

    data class JsonSettings(
        val locale: String = defaultLanguageTag(),
        val background: BackgroundSettings = BackgroundSettings(),
        val opacity: Float = 0.8f,
        val proxy: ProxySettings? = null,
        val disableVerify: Boolean = false
    ) {

        data class BackgroundSettings(
            val imageType: Int = BackgroundImageType.TRANSPARENT,
            val image: String? = null,
        )

        data class ProxySettings(
            val type: Proxy.Type,
            val host: String,
            val port: Int
        )

    }

    private fun releaseSettings() {
        if (!path.exists()) {
            path.toFile().writeText(
                GsonBuilder()
                    .setPrettyPrinting()
                    .create()
                    .toJson(JsonSettings())
            )
        }
    }

    fun save() {
        path.toFile().writeText(
            GsonBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(generateJsonSettings())
        )
    }

    private fun load(): JsonSettings {
        releaseSettings()
        return GsonBuilder()
            .setPrettyPrinting()
            .create()
            .fromJson(path.toFile().readText(), JsonSettings::class.java)
    }

}