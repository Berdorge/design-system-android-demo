package com.vkui.scripts

import com.vkui.scripts.theme.CamelCaseName

fun generateViewThemes(
    pathsRepository: ProjectPathsRepository,
    themes: List<Theme>
) {
    val commonTextStyles = getCommonKeys(themes) { textStyles }
    val commonColors = getCommonKeys(themes) { colors }
    val commonSizes = getCommonKeys(themes) { sizes }
    val commonSpacings = getCommonKeys(themes) { extras.spacings }
    val commonOpacity = getCommonKeys(themes) { opacity }

    generateXmlFile(
        pathsRepository = pathsRepository,
        fileName = "attrs.xml"
    ) {
        for (textStyle in commonTextStyles) {
            writeAttribute(name = textStyle, format = "reference")
        }
        for (color in commonColors) {
            writeAttribute(name = color, format = "color|reference")
        }
        for (size in commonSizes + commonSpacings) {
            writeAttribute(name = size, format = "dimension|reference")
        }
        for (opacity in commonOpacity) {
            writeAttribute(name = opacity, format = "float|reference")
        }
    }
    generateXmlFile(
        pathsRepository = pathsRepository,
        fileName = "styles.xml"
    ) {
        for (theme in themes) {
            for (textStyle in commonTextStyles) {
                writeTextStyle(theme, textStyle)
            }
        }
    }
    generateXmlFile(
        pathsRepository = pathsRepository,
        fileName = "themes.xml"
    ) {
        for (theme in themes) {
            writeStyle(theme.name) {
                for (textStyle in commonTextStyles) {
                    writeTextStyleReference(theme, textStyle)
                }
                for (color in commonColors) {
                    val colorValue = theme.struct.colors.getValue(color).normal
                    writeColor(color, colorValue)
                }
                for (size in commonSizes) {
                    val sizeValue = theme.struct.sizes.getValue(size).regular
                    writeDimension(size, sizeValue)
                }
                for (spacing in commonSpacings) {
                    val spacingValue = theme.struct.extras.spacings.getValue(spacing)
                    writeDimension(spacing, spacingValue)
                }
                for (opacity in commonOpacity) {
                    val opacityValue = theme.struct.opacity.getValue(opacity)
                    writeFloat(opacity, opacityValue)
                }
            }
        }
    }
}

private fun getCommonKeys(
    themes: List<Theme>,
    getMap: ThemeStruct.() -> Map<CamelCaseName, *>
): Set<CamelCaseName> {
    return themes.map { theme ->
        getMap(theme.struct).keys
    }
        .reduceOrNull { first, second -> first.intersect(second) }
        .orEmpty()
}

