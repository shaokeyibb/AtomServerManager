package io.hikarilan.atomservermanager.utils

import io.hikarilan.atomservermanager.io.Client
import okhttp3.Request

object AikarsFlagsUtils {

    private val aikarsFlagFromAikarsWebsitePattern =
        """
            <blockquote class="quoteContainer">
            <div class="quote">
            <p>(java.+?)</p>
            </div>
            </blockquote>
        """.trimIndent().toPattern()

    private val jvmFlagPattern = "-XX:([A-Za-z\\d]+?)=(\\d+)".toRegex()

    fun findAikarsFlagFromAikarsWebsite(): String? {
        val result = Client.okHttpClient.newCall(
            Request.Builder()
                .url("https://aikar.co/mcflags.html")
                .get()
                .build()
        ).execute().body?.string() ?: return null
        val matcher = aikarsFlagFromAikarsWebsitePattern.matcher(result)
        return if (matcher.find()) {
            matcher.group(1)
        } else {
            null
        }
    }

    // -XX:G1NewSizePercent=40
    // -XX:G1MaxNewSizePercent=50
    // -XX:G1HeapRegionSize=16M
    // -XX:G1ReservePercent=15
    // -XX:InitiatingHeapOccupancyPercent=20
    private val modifiableJvmFlags = mapOf(
        "G1NewSizePercent" to "40",
        "G1MaxNewSizePercent" to "50",
        "G1HeapRegionSize" to "16",
        "-G1ReservePercent" to "15",
        "InitiatingHeapOccupancyPercent" to "20"
    )

    fun modifyJVMFlagWhenMemoryGreaterThan12G(aikarsFlag: String): String {
        return aikarsFlag.replace(jvmFlagPattern) {
            val key = it.groupValues[1]
            if (modifiableJvmFlags.containsKey(key)) {
                "-XX:$key=${modifiableJvmFlags[key]}"
            } else {
                it.value
            }
        }
    }

}