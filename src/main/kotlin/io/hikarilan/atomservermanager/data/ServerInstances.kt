package io.hikarilan.atomservermanager.data

import androidx.compose.runtime.mutableStateListOf
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.hikarilan.atomservermanager.servers.Addons
import io.hikarilan.atomservermanager.servers.Cores
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists


object ServerInstances {

    private val path: Path = Paths.get("data.json")

    val instances = mutableStateListOf(*load().toTypedArray())

    data class ServerInstance(
        val core: Cores.Core,
        val version: String,
        val build: String?,
        val addons: List<Addons.Addon>,
        val name: String,
        val commandLine: String
    ) {

        constructor(
            core: String,
            version: String,
            build: String?,
            addons: List<String>,
            name: String,
            commandLine: String
        ) : this(
            core = Cores.allCores.find { it::class.simpleName == core }!!,
            version = version,
            build = build,
            addons = Cores.allCores.find { it::class.simpleName == core }!!.addons.filter { it::class.simpleName in addons },
            name = name,
            commandLine = commandLine
        )


    }

    private fun load(): List<ServerInstance> {
        if (path.exists()) {
            return Gson().fromJson(path.toFile().readText(), JsonArray::class.java).map {
                it.asJsonObject.deserializeToServerInstance()
            }
        }
        return emptyList()
    }

    fun save() {
        path.toFile().writeText(
            GsonBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(JsonArray().apply {
                    instances.forEach { add(it.serializeToJson()) }
                })
        )
    }

}

fun ServerInstances.ServerInstance.serializeToJson(): JsonObject = JsonObject().apply {
    addProperty("core", core::class.simpleName)
    addProperty("version", version)
    addProperty("build", build)
    add("addons", JsonArray().apply {
        addons.forEach { add(it::class.simpleName) }
    })
    addProperty("name", name)
    addProperty("command_line", commandLine)
}

fun JsonObject.deserializeToServerInstance(): ServerInstances.ServerInstance = ServerInstances.ServerInstance(
    core = this.getAsJsonPrimitive("core").asString,
    version = this.getAsJsonPrimitive("version").asString,
    build = this.getAsJsonPrimitive("build").asString,
    addons = this.getAsJsonArray("addons").map { it.asString },
    name = this.getAsJsonPrimitive("name").asString,
    commandLine = this.getAsJsonPrimitive("command_line").asString
)
