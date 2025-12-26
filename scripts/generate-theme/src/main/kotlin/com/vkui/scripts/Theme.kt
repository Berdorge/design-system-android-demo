package com.vkui.scripts

import com.vkui.scripts.theme.CamelCaseName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class Theme(
    val key: ThemeKey,
    val name: CamelCaseName,
    val struct: ThemeStruct,
)

enum class ThemeKey {
    LIGHT,
    DARK
}

@Serializable
data class ThemeStruct(
    @SerialName("font")
    val textStyles: Map<CamelCaseName, TextStyleFlavors>,

    @SerialName("size")
    val sizes: Map<CamelCaseName, SizeFlavors>,

    @SerialName("color")
    val colors: Map<CamelCaseName, ColorFlavors>,

    @SerialName("other")
    val extras: ThemeExtras,

    @SerialName("opacity")
    val opacity: Map<CamelCaseName, Float>,
)

@Serializable
data class TextStyleFlavors(
    @SerialName("regular")
    val regular: TextStyle,
)

@Serializable
data class TextStyle(
    @SerialName("fontSize")
    val fontSize: Int,

    @SerialName("lineHeight")
    val lineHeight: Int,

    @SerialName("fontWeight")
    val fontWeight: Int,

    @SerialName("letterSpacing")
    val letterSpacing: CssPixels? = null,

    @SerialName("textTransform")
    val transform: TextTransform? = null,
) {
    @Serializable
    enum class TextTransform {
        @SerialName("uppercase")
        UPPERCASE,
    }
}

@Serializable
data class SizeFlavors(
    @SerialName("regular")
    val regular: Float,
)

@Serializable
data class ColorFlavors(
    @SerialName("normal")
    val normal: Color,

    @SerialName("hover")
    val hover: Color,

    @SerialName("active")
    val active: Color,
)
