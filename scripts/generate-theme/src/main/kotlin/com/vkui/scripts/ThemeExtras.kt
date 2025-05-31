package com.vkui.scripts

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable(with = ThemeExtrasSerializer::class)
data class ThemeExtras(
    val spacings: Map<String, Int>,
)

object ThemeExtrasSerializer : KSerializer<ThemeExtras> {
    private val jsonObjectSerializer = JsonObject.serializer()

    override val descriptor: SerialDescriptor = jsonObjectSerializer.descriptor

    override fun serialize(encoder: Encoder, value: ThemeExtras) {
        val jsonObject = buildJsonObject {
            value.spacings.forEach { key, value ->
                put(key, value)
            }
        }
        encoder.encodeSerializableValue(jsonObjectSerializer, jsonObject)
    }

    override fun deserialize(decoder: Decoder): ThemeExtras {
        val jsonObject = decoder.decodeSerializableValue(jsonObjectSerializer)
        val spacings = mutableMapOf<String, Int>()
        for ((key, value) in jsonObject.entries) {
            if (key.startsWith("spacing")) {
                val spacing = (value as? JsonPrimitive)?.content?.toIntOrNull()
                if (spacing != null) {
                    spacings[key] = spacing
                }
            }
        }
        return ThemeExtras(
            spacings = spacings,
        )
    }
}
