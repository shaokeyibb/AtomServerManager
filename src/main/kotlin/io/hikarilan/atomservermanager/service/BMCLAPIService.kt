package io.hikarilan.atomservermanager.service

import io.hikarilan.atomservermanager.data.Progress
import io.hikarilan.atomservermanager.data.downloadProgress
import io.hikarilan.atomservermanager.servers.unavailable
import io.hikarilan.atomservermanager.utils.FilesUtils.checkDigest
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.nio.file.Path

object BMCLAPIService {

    sealed class BMCLAPIService {

        abstract val baseLink: String

        fun downloadServer(version: String, saveTo: Path): Progress<File> {
            return downloadProgress(
                name = saveTo.fileName.toString(),
                req = Request.Builder()
                    .get()
                    .url("$baseLink/version/$version/server")
                    .build(),
                saveTo = saveTo,
                checkDigest = { it, it2 -> checkServerDigest(it, it2.toPath()) }
            )
        }

        open fun checkServerDigest(response: Response, saveTo: Path): Result<Boolean?> {
            return response.header("x-bmclapi-hash").let {
                if (it == null) {
                    Result.failure(IOException("No hash found"))
                } else {
                    Result.success(
                        saveTo.toFile()
                            .checkDigest("sha1", it)
                    )
                }
            }
        }

    }


    object BMCLAPIServiceImplOrigin : BMCLAPIService() {

        override val baseLink = "https://bmclapi2.bangbang93.com"

    }

    object BMCLAPIServiceImplMCBBS : BMCLAPIService() {

        override val baseLink = "https://download.mcbbs.net"

        override fun checkServerDigest(response: Response, saveTo: Path): Result<Boolean?> {
            return unavailable()
        }

    }

}