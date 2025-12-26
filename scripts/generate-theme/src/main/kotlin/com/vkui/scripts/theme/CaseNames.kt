package com.vkui.scripts.theme

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class PascalCaseName(val name: String) {
    override fun toString() = name

    fun toCamel() = CamelCaseName(
        name.replaceFirstChar { it.lowercase() }
    )

    fun toSnake() = toCamel().toSnake()
}

@JvmInline
@Serializable
value class CamelCaseName(val name: String) {
    override fun toString() = name

    fun toPascal() = PascalCaseName(
        name.replaceFirstChar { it.uppercase() }
    )

    fun toSnake() = SnakeCaseName(
        name.fold(StringBuilder()) { acc, ch ->
            if (ch.isUpperCase() && acc.isNotEmpty()) {
                acc.append("_${ch.lowercase()}")
            } else {
                acc.append(ch)
            }
        }.toString()
    )
}

@JvmInline
@Serializable
value class SnakeCaseName(val name: String) {
    override fun toString() = name

    fun toPascal() = toCamel().toPascal()

    fun toCamel() = CamelCaseName(
        name.split("_").mapIndexed { i, part ->
            if (i == 0) {
                part
            } else {
                part.replaceFirstChar { it.uppercase() }
            }
        }.joinToString("")
    )
}
