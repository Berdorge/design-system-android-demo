package com.vk.tools.designanalysis

import java.io.File
import java.io.InputStream
import java.security.MessageDigest
import java.util.jar.JarFile

fun forEachClassInJar(jar: File, action: (InputStream) -> Unit) {
    JarFile(jar).use { jarFile ->
        for (jarEntry in jarFile.entries()) {
            if (jarEntry.name.endsWith(".class")) {
                jarFile.getInputStream(jarEntry).use { inputStream ->
                    action(inputStream)
                }
            }
        }
    }
}

fun forEachClassInDir(dir: File, action: (InputStream) -> Unit) {
    dir.walkTopDown()
        .filter { it.isFile && it.extension == "class" }
        .forEach { file ->
            file.inputStream().use { inputStream ->
                action(inputStream)
            }
        }
}

fun File.absolutePathId(): String {
    val normalized = canonicalFile.invariantSeparatorsPath
    val digest = MessageDigest.getInstance("SHA-256")
        .digest(normalized.toByteArray())
    return digest
        .take(8)
        .joinToString("") { "%02x".format(it) }
}
