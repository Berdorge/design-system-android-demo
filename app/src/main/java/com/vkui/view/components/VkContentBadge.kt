package com.vkui.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vkui.compose.components.VkText
import com.vkui.theme.VkTheme

@Composable
fun VkContentBadge(
    text: String,
    appearance: ContentBadgeAppearance,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (appearance) {
        ContentBadgeAppearance.Positive -> VkTheme.colors.colorAccentGreen
        ContentBadgeAppearance.Accent -> VkTheme.colors.colorBackgroundAccent
    }
    VkText(
        text = text,
        color = VkTheme.colors.colorTextContrast,
        style = VkTheme.typography.fontCaption1,
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(VkTheme.dimens.spacingSizeXs))
            .padding(horizontal = VkTheme.dimens.spacingSizeXs, vertical = VkTheme.dimens.spacingSize2Xs)
    )
}

enum class ContentBadgeAppearance {
    Positive,
    Accent
}
