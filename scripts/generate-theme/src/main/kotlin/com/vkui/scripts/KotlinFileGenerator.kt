package com.vkui.scripts

import java.io.BufferedWriter
import java.io.File
import java.nio.file.Path
import kotlin.io.path.outputStream

fun generateTokensKotlinFile(
    sourceSetPath: Path,
    filePackage: String,
    className: String,
    imports: Set<String>,
    properties: List<GeneratedProperty>,
    themedPropertyValues: Map<Theme, (GeneratedProperty) -> String>,
) {
    val vkClassName = "Vk$className"
    sourceSetPath.resolve(filePackage.replace(".", File.separator))
        .resolve("$vkClassName.kt")
        .outputStream()
        .bufferedWriter(Charsets.UTF_8)
        .use { writer ->
            writer.writeLine("package $filePackage")
            writer.writeLine()
            for (import in imports.plus("androidx.compose.runtime.Immutable")) {
                writer.writeLine("import $import")
            }
            writer.writeLine()
            writer.writeLine("@Immutable")
            writer.writeLine("class $vkClassName(")
            for (property in properties) {
                writer.writeLine("    val ${property.name}: ${property.type},")
            }
            writer.writeLine(")")
            writer.writeLine("")
            for ((theme, getPropertyValue) in themedPropertyValues) {
                writer.writeLine("fun ${theme.name}$className(")
                for (property in properties) {
                    writer.writeLine("    ${property.name}: ${property.type} = ${getPropertyValue(property)},")
                }
                writer.writeLine(") = $vkClassName(")
                for (property in properties) {
                    writer.writeLine("    ${property.name} = ${property.name},")
                }
                writer.writeLine(")")
            }
        }
}

private fun BufferedWriter.writeLine(content: String = "") {
    write(content)
    write(System.lineSeparator())
}

data class GeneratedProperty(
    val name: String,
    val type: String,
)
