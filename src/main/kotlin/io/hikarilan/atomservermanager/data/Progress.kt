package io.hikarilan.atomservermanager.data

import io.hikarilan.atomservermanager.io.Client
import io.hikarilan.atomservermanager.servers.unavailable
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.*

abstract class Progress<R> {

    abstract val name: String

    abstract val result: Result<R>

    abstract val checked: Result<Boolean?>

    abstract val progress: Float?

    abstract fun start()

    abstract fun cancel()

    abstract fun isFinished(): Boolean

}

fun downloadProgress(
    name: String,
    req: Request,
    saveTo: Path,
    checkDigest: (Response, File) -> Result<Boolean?> = { _, _ -> unavailable() },
    onFinished: (Progress<*>) -> Unit = {}
): Progress<File> {
    return object : Progress<File>() {

        override val name: String = name

        private val request =
            okhttp3.recipes.Progress.buildProgressClient(Client.okHttpClient) { bytesRead, contentLength, done ->
                progress = bytesRead.toFloat() / contentLength.toFloat()
                if (done){
                    progress = 1f
                }
            }.newCall(req)

        private var stream: InputStream? = null

        override var result: Result<File> = Result.failure(IllegalStateException("Not finished yet"))

        override var checked: Result<Boolean?> = Result.failure(IllegalStateException("Not finished yet"))

        override var progress: Float? = null

        @Throws(IOException::class)
        override fun start() {
            request.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    throw e
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!saveTo.exists()) {
                        saveTo.parent.createDirectories()
                        saveTo.createFile()
                    } else if (checkDigest(response, saveTo.toFile()).let { it.isSuccess && it.getOrNull() != false }) {
                        result = Result.success(saveTo.toFile())
                        finish()
                        checked = Result.success(true)
                        return
                    }
                    stream = response.body!!.byteStream()
                    result = Result.success(
                        saveTo.writeBytes(
                            stream?.readBytes() ?: return
                        ).let {
                            saveTo.toFile()
                        }
                    )
                    response.close()
                    finish()
                    checked = checkDigest(response, saveTo.toFile())
                }
            })
        }

        fun finish(){
            onFinished(this)
        }

        override fun cancel() {
            request.cancel()
            saveTo.deleteIfExists()
        }

        override fun isFinished(): Boolean {
            return result.isSuccess
        }

    }
}