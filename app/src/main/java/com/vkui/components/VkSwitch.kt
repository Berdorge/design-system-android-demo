package com.vkui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vkui.theme.VkTheme

@Composable
fun VkSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val trackPadding = VkTheme.dimens.spacingSize2Xs
    val switchWidth = VkTheme.dimens.sizeSwitchWidth + trackPadding * 2
    val thumbSize = VkTheme.dimens.sizeSwitchPin
    val checkedThumbOffset = switchWidth - thumbSize
    val thumbOffset = animateDpAsState(
        if (checked) {
            checkedThumbOffset
        } else {
            0.dp
        }
    )
    val trackColor = animateColorAsState(
        if (checked) {
            VkTheme.colors.colorBackgroundAccentThemedAlpha
        } else {
            VkTheme.colors.colorIconTertiaryAlpha
        }
    )
    val thumbColor = animateColorAsState(
        if (checked) {
            VkTheme.colors.colorIconAccent
        } else {
            VkTheme.colors.colorIconContrastSecondary
        }
    )

    SwitchVisuals(
        switchWidth = switchWidth,
        trackPadding = trackPadding,
        thumbSize = thumbSize,
        trackColor = trackColor,
        thumbColor = thumbColor,
        thumbOffset = thumbOffset,
        interactionSource = interactionSource,
        modifier = modifier
            .thenIfNotNull(onCheckedChange) { onValueChange ->
                Modifier.toggleable(
                    value = checked,
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    role = Role.Switch,
                    onValueChange = onValueChange
                )
            }
            .thenIf(!enabled) {
                Modifier.alpha(VkTheme.opacity.opacityDisable)
            }
    )
}

@Composable
private fun SwitchVisuals(
    switchWidth: Dp,
    trackPadding: Dp,
    thumbSize: Dp,
    trackColor: State<Color>,
    thumbColor: State<Color>,
    thumbOffset: State<Dp>,
    interactionSource: InteractionSource,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .width(switchWidth)
                .height(VkTheme.dimens.sizeSwitchHeight)
                .padding(horizontal = trackPadding)
                .background(trackColor.value, CircleShape)
        )
        Surface(
            modifier = Modifier
                .offset(x = thumbOffset.value)
                .indication(
                    interactionSource = interactionSource,
                    indication = rememberRipple(bounded = false)
                )
                .size(thumbSize),
            shape = CircleShape,
            color = thumbColor.value,
            shadowElevation = 1.dp,
            content = {}
        )
    }
}
