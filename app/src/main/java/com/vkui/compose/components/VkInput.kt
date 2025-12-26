package com.vkui.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.input.TextFieldValue
import com.vkui.theme.VkTheme

@Composable
fun VkInput(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    placeholderText: String = "",
    leftIcon: Painter? = null,
    leftIconTint: Color = VkTheme.colors.colorIconAccent,
    rightIcon: Painter? = null,
    rightIconTint: Color = VkTheme.colors.colorIconSecondary,
    onRightIconClick: (() -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val isActive by interactionSource.collectIsFocusedAsState()
    VkInputContainer(
        modifier = modifier,
        leftIcon = leftIcon,
        leftIconTint = leftIconTint,
        borderColor = if (isActive) {
            VkTheme.colors.colorStrokeAccent
        } else {
            VkTheme.colors.colorFieldBorderAlpha
        },
        rightIcon = rightIcon,
        rightIconTint = rightIconTint,
        onRightIconClick = onRightIconClick,
    ) {
        if (value.text.isEmpty()) {
            VkText(
                text = placeholderText,
                style = VkTheme.typography.fontText,
                color = VkTheme.colors.colorTextSecondary,
                modifier = Modifier.fillMaxWidth()
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = VkTheme.typography.fontText.textStyle
                .copy(color = VkTheme.colors.colorTextPrimary),
            cursorBrush = SolidColor(VkTheme.colors.colorStrokeAccent),
            interactionSource = interactionSource,
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}

@Composable
fun <V> VkSelectInput(
    selectedValue: V,
    values: ImmutableMap<V, String>,
    onValueSelected: (V) -> Unit,
    modifier: Modifier = Modifier,
    leftIcon: Painter? = null,
    leftIconTint: Color = VkTheme.colors.colorIconAccent,
) {
    var isDropdownShown: Boolean by remember {
        mutableStateOf(false)
    }
    val rightIcon = if (isDropdownShown) {
        Icons.Filled.KeyboardArrowUp
    } else {
        Icons.Filled.KeyboardArrowDown
    }

    Box(modifier = modifier) {
        VkInputContainer(
            leftIcon = leftIcon,
            leftIconTint = leftIconTint,
            rightIcon = rememberVectorPainter(rightIcon),
            modifier = Modifier.clickable {
                isDropdownShown = true
            }
        ) {
            VkText(
                text = values.getValue(selectedValue),
                style = VkTheme.typography.fontText,
                color = VkTheme.colors.colorTextPrimary,
                modifier = Modifier.fillMaxWidth()
            )
        }
        DropdownMenu(
            expanded = isDropdownShown,
            onDismissRequest = { isDropdownShown = false },
        ) {
            for ((value, valueText) in values) {
                DropdownMenuItem(
                    text = {
                        VkText(
                            text = valueText,
                            style = VkTheme.typography.fontFootnote,
                            color = VkTheme.colors.colorTextPrimary
                        )
                    },
                    onClick = {
                        onValueSelected(value)
                        isDropdownShown = false
                    }
                )
            }
        }
    }
}

@Composable
private fun VkInputContainer(
    modifier: Modifier = Modifier,
    leftIcon: Painter? = null,
    leftIconTint: Color = VkTheme.colors.colorIconAccent,
    borderColor: Color = VkTheme.colors.colorFieldBorderAlpha,
    rightIcon: Painter? = null,
    rightIconTint: Color = VkTheme.colors.colorIconSecondary,
    onRightIconClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(VkTheme.dimens.sizeBorderRadius)
    Row(
        modifier = modifier
            .background(VkTheme.colors.colorFieldBackground, shape)
            .border(VkTheme.dimens.sizeBorder2x, borderColor, shape),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leftIcon == null) {
            Spacer(Modifier.size(VkTheme.dimens.spacingSizeXl))
        } else {
            Icon(
                painter = leftIcon,
                tint = leftIconTint,
                contentDescription = null,
                modifier = Modifier.padding(VkTheme.dimens.spacingSizeL)
            )
        }
        Box(
            modifier = Modifier
                .weight(1F)
                .padding(vertical = VkTheme.dimens.sizeBasePaddingVertical)
        ) {
            content()
        }
        if (rightIcon == null) {
            Spacer(Modifier.size(VkTheme.dimens.spacingSizeXl))
        } else {
            Icon(
                painter = rightIcon,
                tint = rightIconTint,
                contentDescription = null,
                modifier = Modifier
                    .clickable(
                        enabled = onRightIconClick != null,
                        onClick = { onRightIconClick?.invoke() },
                        indication = rememberRipple(bounded = false),
                        interactionSource = remember { MutableInteractionSource() }
                    )
                    .padding(VkTheme.dimens.spacingSizeL)
                    .size(VkTheme.dimens.sizeIconUI)
            )
        }
    }
}
