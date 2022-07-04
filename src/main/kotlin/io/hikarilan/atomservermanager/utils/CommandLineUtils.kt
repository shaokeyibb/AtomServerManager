package io.hikarilan.atomservermanager.utils

object CommandLineUtils {

    private val javaCommandLinePattern =
        "((java)(.*?)-Xms(\\d+?[gGmM]) -Xmx(\\d+?[gGmM])(.*?)-jar (.+?.jar) (.*?)(-*nogui)(.*))".toPattern()

    data class JavaCommandLine(
        var javaPath: String = "",
        var jvmOptions: String = "",
        var minMemory: String = "",
        var maxMemory: String = "",
        var jarPath: String = "",
        var applicationOptions: String = ""
    ) {

        constructor(commandLine: String) : this(
            javaPath = "", jvmOptions = "", maxMemory = "", minMemory = "", jarPath = "", applicationOptions = ""
        ) {
            val matcher = javaCommandLinePattern.matcher(commandLine)
            if (matcher.find()) {
                javaPath = matcher.group(2)
                jvmOptions = (matcher.group(3).trim() + " " + matcher.group(6).trim()).trim()
                minMemory = matcher.group(4)
                maxMemory = matcher.group(5)
                jarPath = matcher.group(7)
                applicationOptions = matcher.group(8)
            }
        }

        fun buildCommandLine(): String {
            return "$javaPath ${if (minMemory.isNotBlank()) "-Xms$minMemory" else ""} ${if (maxMemory.isNotBlank()) "-Xmx$maxMemory" else ""} $jvmOptions ${if (jarPath.isNotBlank()) "-jar \"$jarPath\"" else ""} $applicationOptions".trimIndent()
        }

    }

}

fun String.jvmMemoryFlagToBytes(): Long {
    if (this.isBlank() || this.length < 2 || !this.matches("\\d+?[a-zA-Z]".toRegex())) return -1
    val unit = this.substring(this.length - 1)
    val value = this.substring(0, this.length - 1).toLong()
    return when (unit) {
        "g", "G" -> value * 1024 * 1024 * 1024
        "m", "M" -> value * 1024 * 1024
        else -> value
    }
}