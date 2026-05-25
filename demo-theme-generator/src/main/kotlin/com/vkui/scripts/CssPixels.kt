package com.vkui.scripts

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@JvmInline
@Serializable(with = CssPixelsSerializer::class)
value class CssPixels(
    val value: Float
)

object CssPixelsSerializer : KSerializer<CssPixels> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        serialName = "com.vkui.scripts.CssPixels",
        kind = PrimitiveKind.STRING
    )

    override fun serialize(encoder: Encoder, value: CssPixels) {
        encoder.encodeString("${value}px")
    }

    override fun deserialize(decoder: Decoder): CssPixels {
        val stringValue = decoder.decodeString()
        val floatValue = stringValue.removeSuffix("px").toFloat()
        return CssPixels(floatValue)
    }
}
