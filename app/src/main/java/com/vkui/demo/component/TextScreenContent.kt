package com.vkui.demo.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.vkui.compose.components.VkText
import com.vkui.theme.VkTheme

object TextScreenContent {
    @Composable
    fun Content() {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            VkText(
                text = "This is a text",
                style = VkTheme.typography.fontFootnoteCaps,
                color = VkTheme.colors.colorTextPrimary
            )
        }
    }
}
