package io.hikarilan.atomservermanager.data

import com.google.gson.annotations.SerializedName

object ServerJarsData {

    data class JarTypes(
        val response: Response?,
        val status: String
    ) {

        data class Response(
            val bedrock: List<String>,
            val modded: List<String>,
            val proxies: List<String>,
            val servers: List<String>,
            val vanilla: List<String>
        )

    }

    data class AllDetails(
        val response: List<DetailsResponse>,
        val status: String
    )

    data class LatestDetails(
        val response: DetailsResponse,
        val status: String
    )

    data class DetailsResponse(
        val built: Long,
        val file: String,
        val md5: String,
        val stability: Stability,
        val version: String
    ) {

        enum class Stability(val friendlyName: () -> String) {
            @SerializedName("stable")
            STABLE({ io.hikarilan.atomservermanager.i18n.getLang("data.server_jars_data.details_response").asJsonObject["stability.stable"].asString }),

            @SerializedName("unstable")
            UNSTABLE({ io.hikarilan.atomservermanager.i18n.getLang("data.server_jars_data.details_response").asJsonObject["stability.unstable"].asString }),

            @SerializedName("snapshot")
            SNAPSHOT({ io.hikarilan.atomservermanager.i18n.getLang("data.server_jars_data.details_response").asJsonObject["stability.snapshot"].asString })
        }

    }

}