@file:OptIn(ExperimentalStdlibApi::class)

package com.vkui.scripts

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

private val byteHexFormat = HexFormat {
    number.minLength = 2
    number.removeLeadingZeros = false
    upperCase = true
}

@Serializable(with = CssColorSerializer::class)
data class Color(
    val alpha: UByte,
    val red: UByte,
    val green: UByte,
    val blue: UByte,
) {
    fun toRgbaHexBytes() = buildString {
        append(red.toHexString(byteHexFormat))
        append(green.toHexString(byteHexFormat))
        append(blue.toHexString(byteHexFormat))
        append(alpha.toHexString(byteHexFormat))
    }

    fun toArgbHexBytes() = buildString {
        append(alpha.toHexString(byteHexFormat))
        append(red.toHexString(byteHexFormat))
        append(green.toHexString(byteHexFormat))
        append(blue.toHexString(byteHexFormat))
    }

    companion object {
        val TRANSPARENT = Color(UByte.MIN_VALUE, UByte.MIN_VALUE, UByte.MIN_VALUE, UByte.MIN_VALUE)
    }
}

object CssColorSerializer : KSerializer<Color> {
    override val descriptor = PrimitiveSerialDescriptor(
        serialName = "com.vkui.scripts.Color",
        kind = PrimitiveKind.STRING
    )

    private val rgbaRegex = "rgba\\((\\d+),\\s*(\\d+),\\s*(\\d+),\\s*(\\d(\\.\\d+)?)\\)".toRegex()
    private val namedColors = mapOf(
        "transparent" to Color.TRANSPARENT
    )

    override fun serialize(encoder: Encoder, value: Color) {
        val serialized = "#" + value.toRgbaHexBytes()
        encoder.encodeString(serialized)
    }

    override fun deserialize(decoder: Decoder): Color {
        val stringValue = decoder.decodeString()

        namedColors[stringValue]?.let { namedColor ->
            return namedColor
        }

        if (stringValue.startsWith("#")) {
            val hexValue = stringValue.removePrefix("#")
            if (hexValue.length == 6 || hexValue.length == 8) {
                val red = hexValue.substring(0 until 2).hexToUByte(byteHexFormat)
                val green = hexValue.substring(2 until 4).hexToUByte(byteHexFormat)
                val blue = hexValue.substring(4 until 6).hexToUByte(byteHexFormat)
                val alpha = if (hexValue.length == 8) {
                    hexValue.substring(6 until 8).hexToUByte(byteHexFormat)
                } else {
                    UByte.MAX_VALUE
                }
                return Color(alpha, red, green, blue)
            }
        }

        val rgbaMatch = rgbaRegex.matchEntire(stringValue)
        if (rgbaMatch != null) {
            val red = rgbaMatch.groupValues[1].toUByte()
            val green = rgbaMatch.groupValues[2].toUByte()
            val blue = rgbaMatch.groupValues[3].toUByte()
            val alpha = rgbaMatch.groupValues[4].toFloat().times(255).toInt().toUByte()
            return Color(alpha, red, green, blue)
        }

        throw IllegalArgumentException("Unable to deserialize color: $stringValue")
    }
}
