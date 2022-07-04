package io.hikarilan.atomservermanager.servers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import io.hikarilan.atomservermanager.components.NetworkImage
import io.hikarilan.atomservermanager.data.*
import io.hikarilan.atomservermanager.i18n.getLang
import io.hikarilan.atomservermanager.io.Client
import io.hikarilan.atomservermanager.io.Client.addGenericClientHeaders
import io.hikarilan.atomservermanager.utils.FilesUtils.checkDigest
import io.hikarilan.atomservermanager.utils.KnownScaleWrapperContentScale
import io.hikarilan.atomservermanager.utils.format
import okhttp3.Request
import okio.IOException
import java.nio.file.Path
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object DownloadProviders {

    sealed interface DownloadProvider : TechnicalAliasVisitor {

        val name: String

        @Composable
        fun banner(
            modifier: Modifier,
        ) {
            Surface(
                modifier = modifier.fillMaxSize(),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.h4,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        fun available(core: Cores.Core?): Boolean

        // List<String> == null if not available
        fun getVersions(core: Cores.Core): Result<List<String>?>

        fun getBuilds(core: Cores.Core, version: String): Result<List<String>?>

        fun getDetails(core: Cores.Core, version: String?, build: String?): Result<Map<String, String>?>

        fun download(
            root: Path,
            core: Cores.Core,
            version: String?,
            build: String?,
            onFinished: (Progress<*>) -> Unit
        ): Progress<*>

    }

    object PaperMCDownloadProvider : DownloadProvider {

        override val name: String = "PaperMC"

        @Composable
        override fun banner(
            modifier: Modifier,
        ) {
            NetworkImage(
                url = "https://papermc.io/images/logo-marker.svg",
                contentDescription = "PaperMC Banner",
                modifier = modifier,
                contentScale = KnownScaleWrapperContentScale(
                    scale = 100 / 27f,
                    baseContentScale = ContentScale.Fit
                )
            )
        }

        @Throws(IOException::class)
        private fun getSupportProjects(): PaperMCData.Projects {
            return Client.okHttpClient.newCall(
                Request.Builder()
                    .url("https://papermc.io/api/v2/projects")
                    .get()
                    .build()
            ).execute().body?.string()?.let {
                Gson().fromJson(it, PaperMCData.Projects::class.java)
            } ?: PaperMCData.Projects(listOf())
        }

        private fun getBuild(core: Cores.Core, version: String?, build: String?): PaperMCData.Build {
            var url = "https://papermc.io/api/v2/projects/${
                core.getTechnicalName(this).lowercase()
            }/versions/$version/builds/$build"
            if (version == null) {
                val ver = getVersions(core).getOrThrow()[0]
                val bud = getBuilds(core, ver).getOrThrow()[0]
                url = "https://papermc.io/api/v2/projects/${
                    core.getTechnicalName(this).lowercase()
                }/versions/$ver/builds/$bud"
            } else if (build == null) {
                val bud = getBuilds(core, version).getOrThrow()[0]
                url = "https://papermc.io/api/v2/projects/${
                    core.getTechnicalName(this).lowercase()
                }/versions/$version/builds/$bud"
            }
            return Client.okHttpClient.newCall(
                Request.Builder()
                    .url(url)
                    .get()
                    .build()
            ).execute().body?.string().let { Gson().fromJson(it, PaperMCData.Build::class.java) }
        }

        override fun available(core: Cores.Core?): Boolean {
            try {
                getSupportProjects()
            } catch (e: IOException) {
                return false
            }
            return true
        }

        override fun getVersions(core: Cores.Core): Result<List<String>> {
            return Client.okHttpClient.newCall(
                Request.Builder()
                    .url("https://papermc.io/api/v2/projects/${core.getTechnicalName(this).lowercase()}")
                    .get()
                    .build()
            ).execute().body?.string().let {
                try {
                    Result.success(Gson().fromJson(it, PaperMCData.Project::class.java).versions.reversed())
                } catch (e: Throwable) {
                    Result.failure(e)
                }
            }
        }

        override fun getBuilds(core: Cores.Core, version: String): Result<List<String>> {
            return Client.okHttpClient.newCall(
                Request.Builder()
                    .url(
                        "https://papermc.io/api/v2/projects/${
                            core.getTechnicalName(this).lowercase()
                        }/versions/$version"
                    )
                    .get()
                    .build()
            ).execute().body?.string().let {
                try {
                    Result.success(
                        Gson().fromJson(
                            it,
                            PaperMCData.Version::class.java
                        ).builds.map { ver -> ver.toString() }.reversed()
                    )
                } catch (e: Throwable) {
                    Result.failure(e)
                }
            }
        }

        override fun getDetails(core: Cores.Core, version: String?, build: String?): Result<Map<String, String>> {
            return try {
                val dataLang = getLang("data.paper_mc_data.build").asJsonObject
                Result.success(
                    getBuild(core, version, build).let { data ->
                        mapOf(
                            dataLang["project_name"].asString to data.project_name,
                            dataLang["version"].asString to data.version,
                            dataLang["build"].asString to data.build.toString(),
                            dataLang["time"].asString to DateTimeFormatter.ISO_INSTANT
                                .withZone(ZoneId.ofOffset("UTC", ZoneOffset.UTC))
                                .parse(data.time).format(),
                            dataLang["channel"].asString to data.channel.friendlyName(),
                            dataLang["changes"].asString to data.changes.joinToString(separator = "\n") { change -> "#${change.commit}\n${change.message}" },
                            dataLang["download.application"].asJsonObject["name"].asString to data.downloads.application.name,
                            dataLang["download.application"].asJsonObject["sha256"].asString to data.downloads.application.sha256,
                        )
                    }
                )
            } catch (e: Throwable) {
                e.printStackTrace()
                Result.failure(e)
            }
        }

        override fun download(
            root: Path,
            core: Cores.Core,
            version: String?,
            build: String?,
            onFinished: (Progress<*>) -> Unit
        ): Progress<*> {
            val details = getBuild(core, version, build)
            return downloadProgress(
                name = details.downloads.application.name,
                req = Request.Builder()
                    .get()
                    .url(
                        "https://papermc.io/api/v2/projects/${details.project_id}/versions/${details.version}/builds/${details.build}/downloads/${details.downloads.application.name}"
                    )
                    .build(),
                saveTo = root.resolve(details.downloads.application.name),
                checkDigest = { _, file ->
                    Result.success(file.checkDigest("sha256", details.downloads.application.sha256))
                },
                onFinished = onFinished
            )
        }
    }

    object ServerJarsDownloadProvider : DownloadProvider {

        override val name: String = "ServerJars"

        @Composable
        override fun banner(
            modifier: Modifier,
        ) {
            NetworkImage(
                url = "https://serverjars.com/img/logo_white.svg",
                contentDescription = "ServerJars Banner",
                modifier = modifier,
                contentScale = KnownScaleWrapperContentScale(
                    scale = 450 / 103f,
                    baseContentScale = ContentScale.Fit
                )
            )
        }

        @Throws(IOException::class, JsonSyntaxException::class)
        private fun getJarTypes(type: String = ""): ServerJarsData.JarTypes {
            return Client.okHttpClient.newCall(
                Request.Builder()
                    .url("https://serverjars.com/api/fetchTypes/$type")
                    .get()
                    .build()
            ).execute().body?.string()?.let {
                Gson().fromJson(it, ServerJarsData.JarTypes::class.java)
            } ?: ServerJarsData.JarTypes(
                response = null,
                status = "error"
            )
        }

        @Throws(IOException::class, JsonSyntaxException::class)
        private fun getAllDetails(core: Cores.Core): ServerJarsData.AllDetails {
            return Client.okHttpClient.newCall(
                Request.Builder()
                    .url("https://serverjars.com/api/fetchAll/${core.getTechnicalName(this)}/1000")
                    .get()
                    .build()
            ).execute().body?.string().let {
                Gson().fromJson(
                    it,
                    ServerJarsData.AllDetails::class.java
                )
            }
        }

        @Throws(IOException::class, JsonSyntaxException::class)
        private fun getLatestDetails(core: Cores.Core): ServerJarsData.LatestDetails {
            return Client.okHttpClient.newCall(
                Request.Builder()
                    .url("https://serverjars.com/api/fetchLatest/${core.getTechnicalName(this)}")
                    .get()
                    .build()
            ).execute().body?.string().let {
                Gson().fromJson(
                    it,
                    ServerJarsData.LatestDetails::class.java
                )
            }
        }

        private fun checkDigest(root: Path, core: Cores.Core, version: String?): Result<Boolean?> {
            return Result.success(
                root.resolve(getAllDetails(core).response.find { version == it.version }!!.file).toFile()
                    .checkDigest("md5", getAllDetails(core).response.find { version == it.version }!!.md5)
            )
        }

        override fun available(core: Cores.Core?): Boolean {
            try {
                getJarTypes()
            } catch (e: IOException) {
                return false
            }
            return true
        }

        override fun getVersions(core: Cores.Core): Result<List<String>> {
            return try {
                Result.success(getAllDetails(core).response.map { response -> response.version })
            } catch (e: Throwable) {
                Result.failure(e)
            }
        }

        override fun getBuilds(core: Cores.Core, version: String): Result<List<String>?> = unavailable()

        override fun getDetails(core: Cores.Core, version: String?, build: String?): Result<Map<String, String>?> {
            try {
                val dataLang = getLang("data.server_jars_data.details_response").asJsonObject
                return Result.success(
                    (getAllDetails(core).response.find { version == it.version }
                        ?: getLatestDetails(core).response).let { data ->
                        mapOf(
                            dataLang["version"].asString to data.version,
                            dataLang["built"].asString to Instant.ofEpochMilli(data.built).format(),
                            dataLang["stability"].asString to data.stability.friendlyName(),
                            dataLang["file"].asString to data.file,
                            dataLang["md5"].asString to data.md5,
                        )
                    }
                )
            } catch (e: Throwable) {
                return Result.failure(e)
            }
        }

        override fun download(
            root: Path,
            core: Cores.Core,
            version: String?,
            build: String?,
            onFinished: (Progress<*>) -> Unit
        ): Progress<*> {
            val file = root.resolve(getAllDetails(core).response.find { version == it.version }!!.file)
            return downloadProgress(
                name = file.fileName.toString(),
                req = Request.Builder()
                    .get()
                    .url("https://serverjars.com/api/fetchJar/${core.getTechnicalName(this)}/$version")
                    .build(),
                saveTo = file,
                checkDigest = { _, _ ->
                    checkDigest(root, core, version)
                },
                onFinished = onFinished
            )
        }
    }

    object SpongeDownloadProvider : DownloadProvider {
        override val name: String = "SpongePowered"

        override fun available(core: Cores.Core?): Boolean {
            try {
                Client.okHttpClient.newCall(
                    Request.Builder()
                        .url("https://www.spongepowered.org/downloads/${core?.technicalAlias?.get(this)}")
                        .get()
                        .addGenericClientHeaders()
                        .build()
                ).execute().close()
            } catch (e: IOException) {
                return false
            }
            return true
        }

        @Throws(IOException::class, JsonSyntaxException::class)
        private fun getArtifacts(core: Cores.Core): SpongeData.Artifacts {
            return Client.okHttpClient.newCall(
                Request.Builder()
                    .url("https://dl-api-new.spongepowered.org/api/v2/groups/org.spongepowered/artifacts/${core.technicalAlias[this]}")
                    .get()
                    .addGenericClientHeaders()
                    .build()
            ).execute().body?.string()?.let {
                Gson().fromJson(
                    it,
                    SpongeData.Artifacts::class.java
                )
            } ?: throw IOException("Failed to get artifacts")
        }

        override fun getVersions(core: Cores.Core): Result<List<String>?> {
            return try {
                Result.success(getArtifacts(core).tags.minecraft)
            } catch (e: IOException) {
                Result.failure(e)
            }
        }

        @Throws(IOException::class, JsonSyntaxException::class)
        private fun getVersions(core: Cores.Core, version: String): SpongeData.Builds {
            return Client.okHttpClient.newCall(
                Request.Builder()
                    .url("https://dl-api-new.spongepowered.org/api/v2/groups/org.spongepowered/artifacts/${core.technicalAlias[this]}/versions?tags=minecraft:$version&offset=0")
                    .get()
                    .addGenericClientHeaders()
                    .build()
            ).execute().body?.string()?.let {
                Gson().fromJson(
                    it,
                    SpongeData.Builds::class.java
                )
            } ?: throw IOException("Failed to get versions")
        }

        override fun getBuilds(core: Cores.Core, version: String): Result<List<String>?> {
            return try {
                Result.success(getVersions(core, version).artifacts.entries.sortedBy { !it.value.recommended }
                    .map { it.key })
            } catch (e: IOException) {
                Result.failure(e)
            }
        }

        private fun getDetail(core: Cores.Core, build: String): SpongeData.Versions {
            return Client.okHttpClient.newCall(
                Request.Builder()
                    .url("https://dl-api-new.spongepowered.org/api/v2/groups/org.spongepowered/artifacts/${core.technicalAlias[this]}/versions/$build")
                    .get()
                    .addGenericClientHeaders()
                    .build()
            ).execute().body?.string()?.let {
                Gson().fromJson(
                    it,
                    SpongeData.Versions::class.java
                )
            } ?: throw IOException("Failed to get detail")
        }

        override fun getDetails(core: Cores.Core, version: String?, build: String?): Result<Map<String, String>?> {
            return try {
                val data = getDetail(core, build ?: return Result.success(null))
                val dataLang = getLang("data.server_jars_data.sponge_detail").asJsonObject
                Result.success(
                    buildMap {
                        put(dataLang["group"].asString, data.coordinates.groupId)
                        put(dataLang["artifact"].asString, data.coordinates.artifactId)
                        put(dataLang["version"].asString, data.coordinates.version)
                        put(dataLang["minecraft"].asString, data.tags.minecraft)
                        put(dataLang["api"].asString, data.tags.api)
                        data.tags.forge?.let { put(dataLang["forge"].asString, it) }
                        data.commit?.let {
                            put(
                                dataLang["commits"].asString,
                                it.commits.joinToString(separator = "\n") { change -> change.commit.message }
                            )
                        }
                        put(dataLang["recommend"].asString, data.recommended.toString())
                    }
                )
            } catch (e: Throwable) {
                return Result.failure(e)
            }
        }

        override fun download(
            root: Path,
            core: Cores.Core,
            version: String?,
            build: String?,
            onFinished: (Progress<*>) -> Unit
        ): Progress<*> {
            val detail = getDetail(core, build!!).assets.find { it.extension == "jar" && it.classifier == "" }!!
            return downloadProgress(
                name = detail.downloadUrl.substringAfterLast("/"),
                req = Request.Builder()
                    .get()
                    .url(detail.downloadUrl)
                    .addGenericClientHeaders()
                    .build(),
                checkDigest = { _, file ->
                    Result.success(file.checkDigest("md5", detail.md5) && file.checkDigest("sha1", detail.sha1))
                },
                saveTo = root.resolve(detail.downloadUrl.substringAfterLast("/")),
                onFinished = onFinished
            )
        }

    }

}

fun <T> unavailable() = Result.success<T?>(null)
