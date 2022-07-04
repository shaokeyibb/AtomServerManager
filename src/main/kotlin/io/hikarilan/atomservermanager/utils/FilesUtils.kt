package io.hikarilan.atomservermanager.utils

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.util.StringJoiner

object FilesUtils {

    fun Path.isImage() = Files.probeContentType(this)?.split("/")?.get(0) == "image"

    fun File.checkDigest(type: String, digest: String): Boolean {
        MessageDigest.getInstance(type).digest(this.readBytes()).let {
            return StringJoiner("").apply { it.forEach { add(String.format("%02x", it)) } }.toString() == digest
        }
    }

}