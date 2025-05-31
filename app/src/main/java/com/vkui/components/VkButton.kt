package com.vkui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.vkui.theme.VkTheme

@Composable
fun VkButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = VkTheme.colors.colorTransparent,
        modifier = modifier.heightIn(min = VkTheme.dimens.sizeButtonMediumHeight)
    ) {
        Box(contentAlignment = Alignment.Center) {
            VkText(
                text = text,
                style = VkTheme.typography.fontHeadline2,
                color = VkTheme.colors.colorTextAccent,
                modifier = Modifier.padding(horizontal = VkTheme.dimens.spacingSizeXl)
            )
        }
    }
}
