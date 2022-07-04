package io.hikarilan.atomservermanager.data

import com.google.gson.annotations.SerializedName
import io.hikarilan.atomservermanager.i18n.getLang

object PaperMCData {

    data class Projects(
        val projects: List<String>
    )

    data class Project(
        val project_id: String,
        val project_name: String,
        val version_groups: List<String>,
        val versions: List<String>
    )

    data class Version(
        val builds: List<Int>,
        val project_id: String,
        val project_name: String,
        val version: String
    )

    data class Build(
        val project_id: String,
        val project_name: String,
        val version: String,
        val build: Int,
        val time: String,
        val channel: Channel,
        val promoted: Boolean,
        val changes: List<Change>,
        val downloads: Downloads,
    ) {

        enum class Channel(val friendlyName: ()->String) {
            @SerializedName("default")
            DEFAULT({getLang("data.paper_mc_data.build").asJsonObject["channel.default"].asString}),

            @SerializedName("experimental")
            EXPERIMENTAL({getLang("data.paper_mc_data.build").asJsonObject["channel.default"].asString})
        }

        data class Change(
            val commit: String,
            val message: String,
            val summary: String
        )

        data class Downloads(
            val application: Application,
            @SerializedName("mojang-mappings")
            val mojangMappings: MojangMappings?
        )

        data class Application(
            val name: String,
            val sha256: String
        )

        data class MojangMappings(
            val name: String,
            val sha256: String
        )

    }

}