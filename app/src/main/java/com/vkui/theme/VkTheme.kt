package com.vkui.theme

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle

val LocalVkTextStyle = compositionLocalOf {
    VkTextStyle(
        textTransform = VkTextTransform.NONE,
        textStyle = TextStyle.Default
    )
}

private val LocalVkColorScheme = staticCompositionLocalOf { vkontakteAndroidColorScheme() }
private val LocalVkTypography = staticCompositionLocalOf { vkontakteAndroidTypography() }
private val LocalVkDimens = staticCompositionLocalOf { vkontakteAndroidDimens() }
private val LocalVkOpacity = staticCompositionLocalOf { vkontakteAndroidOpacity() }

object VkTheme {
    val colors
        @Composable
        get() = LocalVkColorScheme.current

    val typography
        @Composable
        get() = LocalVkTypography.current

    val dimens
        @Composable
        get() = LocalVkDimens.current

    val opacity
        @Composable
        get() = LocalVkOpacity.current
}

@Composable
fun VkTheme(
    isDark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = remember(isDark) {
        if (isDark) {
            vkontakteAndroidDarkColorScheme()
        } else {
            vkontakteAndroidColorScheme()
        }
    }
    val typography = remember(isDark) {
        if (isDark) {
            vkontakteAndroidDarkTypography()
        } else {
            vkontakteAndroidTypography()
        }
    }
    val dimens = remember(isDark) {
        if (isDark) {
            vkontakteAndroidDarkDimens()
        } else {
            vkontakteAndroidDimens()
        }
    }
    val opacity = remember(isDark) {
        if (isDark) {
            vkontakteAndroidDarkOpacity()
        } else {
            vkontakteAndroidOpacity()
        }
    }

    CompositionLocalProvider(
        LocalVkColorScheme provides colorScheme,
        LocalVkTypography provides typography,
        LocalVkDimens provides dimens,
        LocalVkOpacity provides opacity,
        LocalIndication provides rememberRipple(),
        content = content
    )
}
