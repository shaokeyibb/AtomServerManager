package io.hikarilan.atomservermanager.data

import com.google.gson.annotations.SerializedName


object SpongeData {

    data class Artifacts(
        val coordinates: Coordinates,
        val displayName: String,
        val gitRepository: String,
        val issues: Any,
        val tags: Tags,
        val type: String,
        val website: Any
    ) {

        data class Coordinates(
            val artifactId: String, val groupId: String
        )

        data class Tags(
            val api: List<String>, val forge: List<String>, val minecraft: List<String>
        )
    }

    data class Builds(
        val artifacts: Map<String, Artifact>, val limit: Int, val offset: Int, val size: Int
    ) {

        data class Artifact(
            val tagValues: TagValues, val recommended: Boolean
        ) {

            data class TagValues(
                val minecraft: String, val forge: String, val api: String
            )

        }

    }

    data class Versions(
        @SerializedName("assets")
        val assets: List<Asset>,
        @SerializedName("commit")
        val commit: Commit?,
        @SerializedName("coordinates")
        val coordinates: Coordinates,
        @SerializedName("recommended")
        val recommended: Boolean,
        @SerializedName("tags")
        val tags: Tags
    ) {
        data class Asset(
            @SerializedName("classifier")
            val classifier: String,
            @SerializedName("downloadUrl")
            val downloadUrl: String,
            @SerializedName("extension")
            val extension: String,
            @SerializedName("md5")
            val md5: String,
            @SerializedName("sha1")
            val sha1: String
        )

        data class Commit(
            @SerializedName("commits")
            val commits: List<Commit>,
            @SerializedName("processing")
            val processing: Boolean
        ) {
            data class Commit(
                @SerializedName("commit")
                val commit: Commit,
                @SerializedName("submoduleCommits")
                val submoduleCommits: List<Any>
            ) {
                data class Commit(
                    @SerializedName("author")
                    val author: Author,
                    @SerializedName("body")
                    val body: String,
                    @SerializedName("commitDate")
                    val commitDate: String,
                    @SerializedName("commiter")
                    val commiter: Commiter,
                    @SerializedName("link")
                    val link: String,
                    @SerializedName("message")
                    val message: String,
                    @SerializedName("sha")
                    val sha: String
                ) {
                    data class Author(
                        @SerializedName("email")
                        val email: String,
                        @SerializedName("name")
                        val name: String
                    )

                    data class Commiter(
                        @SerializedName("email")
                        val email: String,
                        @SerializedName("name")
                        val name: String
                    )
                }
            }
        }

        data class Coordinates(
            @SerializedName("artifactId")
            val artifactId: String,
            @SerializedName("groupId")
            val groupId: String,
            @SerializedName("version")
            val version: String
        )

        data class Tags(
            @SerializedName("api")
            val api: String,
            @SerializedName("forge")
            val forge: String?,
            @SerializedName("minecraft")
            val minecraft: String
        )
    }

}