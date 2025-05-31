package com.vkui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.vkui.theme.LocalVkTextStyle
import com.vkui.theme.VkTextStyle
import com.vkui.theme.VkTextTransform

@Composable
fun VkText(
    text: String,
    modifier: Modifier = Modifier,
    style: VkTextStyle = LocalVkTextStyle.current,
    color: Color = Color.Unspecified,
) {
    Text(
        text = when (style.textTransform) {
            VkTextTransform.NONE -> text
            VkTextTransform.UPPERCASE -> text.uppercase()
        },
        modifier = modifier,
        style = style.textStyle,
        color = color
    )
}
