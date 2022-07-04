package io.hikarilan.atomservermanager.io

import io.hikarilan.atomservermanager.Settings
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.Proxy

object Client {

    val okHttpClient = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .proxy(Settings.proxy ?: Proxy.NO_PROXY)
        .build()

    val okHttpClientWithNoAutoRedirect = OkHttpClient.Builder()
        .proxy(Settings.proxy ?: Proxy.NO_PROXY)
        .build()

    fun Request.Builder.addGenericClientHeaders():Request.Builder {
        addHeader("sec-ch-ua", "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"100\", \"Microsoft Edge\";v=\"100\"")
        addHeader(
            "user-agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.127 Safari/537.36 Edg/100.0.1185.44 AtomServerManger/1.0.0"
        )
        return this
    }

}