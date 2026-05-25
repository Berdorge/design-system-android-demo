package com.vkui.scripts

import com.vkui.scripts.theme.CamelCaseName
import kotlin.io.path.outputStream

fun generateXmlFile(
    pathsRepository: ProjectPathsRepository,
    fileName: String,
    action: XmlFileWriter.() -> Unit
) {
    val resourcesPath = pathsRepository.getThemeGenerationResourcesPath()
    resourcesPath.resolve("values")
        .resolve(fileName)
        .outputStream()
        .bufferedWriter(Charsets.UTF_8)
        .use { writer ->
            val existingStyles = mutableSetOf<String>()

            fun writeStyle(name: String, action: XmlStyleWriter.() -> Unit) {
                val styleParents = name.split('.').dropLast(1)
                    .scan("") { acc, part ->
                        if (acc.isEmpty()) part else "$acc.$part"
                    }
                    .filter { it.isNotEmpty() }
                for (parent in styleParents) {
                    if (existingStyles.add(parent)) {
                        writer.writeLine()
                        writer.writeLine("""    <style name="$parent" />""")
                    }
                }
                writer.writeLine()
                writer.writeLine("""    <style name="$name">""")
                val xmlStyleWriter = object : XmlStyleWriter {
                    override fun writeTextStyleReference(theme: Theme, name: CamelCaseName) {
                        val pascalTheme = theme.name.toPascal()
                        val pascalTextStyle = name.toPascal()
                        val snakeTextStyle = name.toSnake()
                        writer.writeLine(
                            """        <item name="vk_ui_$snakeTextStyle">@style/VkUiTextStyle.${pascalTheme}.${pascalTextStyle}</item>"""
                        )
                    }

                    override fun writeColor(name: CamelCaseName, color: Color) {
                        val snakeName = name.toSnake()
                        writer.writeLine(
                            """        <item name="vk_ui_$snakeName">#${color.toArgbHexBytes()}</item>"""
                        )
                    }

                    override fun writeDimension(name: CamelCaseName, dimension: Number) {
                        val snakeName = name.toSnake()
                        writer.writeLine(
                            """        <item name="vk_ui_$snakeName">${dimension}dp</item>"""
                        )
                    }

                    override fun writeFloat(name: CamelCaseName, float: Float) {
                        val snakeName = name.toSnake()
                        writer.writeLine(
                            """        <item name="vk_ui_$snakeName">$float</item>"""
                        )
                    }
                }
                xmlStyleWriter.action()
                writer.writeLine("    </style>")
            }

            writer.writeLine("""<?xml version="1.0" encoding="utf-8" ?>""")
            writer.writeLine("<resources>")
            val xmlFileWriter = object : XmlFileWriter {
                override fun writeAttribute(name: CamelCaseName, format: String) {
                    val snakeName = name.toSnake()
                    writer.writeLine("""    <attr name="vk_ui_${snakeName}" format="$format" />""")
                }

                override fun writeTextStyle(theme: Theme, name: CamelCaseName) {
                    val pascalTheme = theme.name.toPascal()
                    val pascalTextStyle = name.toPascal()
                    val styleValue = theme.struct.textStyles.getValue(name).regular
                    writeStyle("VkUiTextStyle.${pascalTheme}.${pascalTextStyle}") {
                        writer.writeLine(
                            """        <item name="android:textSize">${styleValue.fontSize}sp</item>"""
                        )
                        writer.writeLine(
                            """        <item name="android:lineHeight">${styleValue.lineHeight}sp</item>"""
                        )
                        writer.writeLine(
                            """        <item name="android:fontWeight">${styleValue.fontWeight}</item>"""
                        )
                        if (styleValue.letterSpacing != null) {
                            writer.writeLine(
                                """        <item name="android:letterSpacing">${styleValue.letterSpacing.value / styleValue.fontSize}</item>"""
                            )
                        }
                        val textAllCaps = when (styleValue.transform) {
                            TextStyle.TextTransform.UPPERCASE -> true
                            null -> false
                        }
                        writer.writeLine(
                            """        <item name="android:textAllCaps">$textAllCaps</item>"""
                        )
                    }
                }

                override fun writeStyle(name: CamelCaseName, action: XmlStyleWriter.() -> Unit) {
                    val pascalName = name.toPascal()
                    val styleName = "VkUiTheme.${pascalName}Generated"
                    writeStyle(styleName, action)
                }
            }
            xmlFileWriter.action()
            writer.writeLine("</resources>")
        }
}

interface XmlFileWriter {
    fun writeAttribute(name: CamelCaseName, format: String)
    fun writeTextStyle(theme: Theme, name: CamelCaseName)
    fun writeStyle(name: CamelCaseName, action: XmlStyleWriter.() -> Unit)
}

interface XmlStyleWriter {
    fun writeTextStyleReference(theme: Theme, name: CamelCaseName)
    fun writeColor(name: CamelCaseName, color: Color)
    fun writeDimension(name: CamelCaseName, dimension: Number)
    fun writeFloat(name: CamelCaseName, float: Float)
}
