package com.vkui.scripts

private const val GENERATED_FILES_PACKAGE = "com.vkui.theme"

suspend fun main() {
    val themesRepository = createThemesRepository()
    val pathsRepository = createPathsRepository()
    val themes = ThemeKey.entries.map { themeKey ->
        themesRepository.getTheme(themeKey)
    }
    val generationPath = pathsRepository.getThemeGenerationSourceSetPath()

    fun getTokenKeys(getMap: ThemeStruct.() -> Map<String, *>) = themes.map { theme ->
        getMap(theme.struct).keys
    }
        .reduceOrNull { first, second -> first.intersect(second) }
        .orEmpty()

    fun getThemedPropertyValues(getPropertyValue: (ThemeStruct, GeneratedProperty) -> String) =
        themes.associateWith { (_, _, struct) ->
            { property: GeneratedProperty ->
                getPropertyValue(struct, property)
            }
        }

    val commonTextStyles = getTokenKeys { textStyles }
    val commonColors = getTokenKeys { colors }
    val commonSizes = getTokenKeys { sizes }
    val commonSpacings = getTokenKeys { extras.spacings }
    val commonOpacity = getTokenKeys { opacity }

    generateTokensKotlinFile(
        sourceSetPath = generationPath,
        filePackage = GENERATED_FILES_PACKAGE,
        className = "ColorScheme",
        imports = setOf("androidx.compose.ui.graphics.Color"),
        properties = commonColors.map { color ->
            GeneratedProperty(name = color, type = "Color")
        },
        themedPropertyValues = getThemedPropertyValues { struct, property ->
            val color = struct.colors.getValue(property.name).normal
            "Color(0x${color.toArgbHexBytes()})"
        }
    )
    generateTokensKotlinFile(
        sourceSetPath = generationPath,
        filePackage = GENERATED_FILES_PACKAGE,
        className = "Typography",
        imports = setOf(
            "androidx.compose.ui.text.TextStyle",
            "androidx.compose.ui.text.font.FontWeight",
            "androidx.compose.ui.unit.TextUnit",
            "androidx.compose.ui.unit.sp"
        ),
        properties = commonTextStyles.map { textStyle ->
            GeneratedProperty(name = textStyle, type = "VkTextStyle")
        },
        themedPropertyValues = getThemedPropertyValues { struct, property ->
            val style = struct.textStyles.getValue(property.name).regular
            val textTransform = when (style.transform) {
                TextStyle.TextTransform.UPPERCASE -> "VkTextTransform.UPPERCASE"
                null -> "VkTextTransform.NONE"
            }
            val letterSpacing = if (style.letterSpacing == null) {
                "TextUnit.Unspecified"
            } else {
                "${style.letterSpacing.value}.sp"
            }
            val textStyle = "TextStyle(" +
                "fontWeight = FontWeight(${style.fontWeight}), " +
                "fontSize = ${style.fontSize}.sp, " +
                "lineHeight = ${style.lineHeight}.sp, " +
                "letterSpacing = $letterSpacing, " +
                ")"
            "VkTextStyle(textTransform = $textTransform, textStyle = $textStyle)"
        }
    )
    generateTokensKotlinFile(
        sourceSetPath = generationPath,
        filePackage = GENERATED_FILES_PACKAGE,
        className = "Dimens",
        imports = setOf(
            "androidx.compose.ui.unit.Dp",
            "androidx.compose.ui.unit.dp"
        ),
        properties = (commonSizes + commonSpacings).map { name ->
            GeneratedProperty(name, "Dp")
        },
        themedPropertyValues = getThemedPropertyValues { struct, property ->
            val value = struct.sizes[property.name]?.regular
                ?: struct.extras.spacings.getValue(property.name)
            "$value.dp"
        }
    )
    generateTokensKotlinFile(
        sourceSetPath = generationPath,
        filePackage = GENERATED_FILES_PACKAGE,
        className = "Opacity",
        imports = emptySet(),
        properties = commonOpacity.map { name ->
            GeneratedProperty(name, "Float")
        },
        themedPropertyValues = getThemedPropertyValues { struct, property ->
            val value = struct.opacity.getValue(property.name)
            "${value}F"
        }
    )
}
