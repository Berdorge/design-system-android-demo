package com.vkui.theme

import androidx.compose.runtime.Immutable

@Immutable
class VkOpacity(
    val opacityDisable: Float,
    val opacityDisableAccessibility: Float,
    val opacityActive: Float,
)

fun vkontakteAndroidOpacity(
    opacityDisable: Float = 0.4F,
    opacityDisableAccessibility: Float = 0.64F,
    opacityActive: Float = 0.72F,
) = VkOpacity(
    opacityDisable = opacityDisable,
    opacityDisableAccessibility = opacityDisableAccessibility,
    opacityActive = opacityActive,
)
fun vkontakteAndroidDarkOpacity(
    opacityDisable: Float = 0.4F,
    opacityDisableAccessibility: Float = 0.64F,
    opacityActive: Float = 0.72F,
) = VkOpacity(
    opacityDisable = opacityDisable,
    opacityDisableAccessibility = opacityDisableAccessibility,
    opacityActive = opacityActive,
)
