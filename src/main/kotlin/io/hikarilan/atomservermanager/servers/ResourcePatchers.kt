package io.hikarilan.atomservermanager.servers

import io.hikarilan.atomservermanager.data.Progress
import io.hikarilan.atomservermanager.service.BMCLAPIService
import java.io.File
import java.nio.file.Path

object ResourcePatchers {

    sealed interface ResourcePatcher {

        val name: String

        fun execute(root: Path, version: String?, build: String?): Progress<*>

    }

    sealed class BMCLAPIPaperResourcePatcher : ResourcePatcher {

        abstract val service: BMCLAPIService.BMCLAPIService

        override fun execute(root: Path, version: String?, build: String?): Progress<File> {
            return service.downloadServer(version!!, root.resolve("cache").resolve("mojang_$version.jar"))
        }

    }

    object BMCLAPIOriginPaperResourcePatcher : BMCLAPIPaperResourcePatcher() {

        override val name: String = "BMCLAPI"

        override val service: BMCLAPIService.BMCLAPIService = BMCLAPIService.BMCLAPIServiceImplOrigin

    }

    object BMCLAPIMCBBSPaperResourcePatcher : BMCLAPIPaperResourcePatcher() {

        override val name: String = "BMCLAPI(MCBBS)"

        override val service: BMCLAPIService.BMCLAPIService = BMCLAPIService.BMCLAPIServiceImplMCBBS

    }

}