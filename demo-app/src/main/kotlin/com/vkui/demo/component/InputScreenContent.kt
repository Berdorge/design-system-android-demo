package com.vkui.demo.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import com.vkui.compose.components.VkInput
import com.vkui.compose.theme.VkTheme
import com.vkui.icons.R as IconsR

object InputScreenContent {
    private val content = mutableStateOf(TextFieldValue())

    @Composable
    fun Content() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(
                    horizontal = VkTheme.dimens.sizeBasePaddingHorizontal,
                    vertical = VkTheme.dimens.sizeBasePaddingVertical
                )
        ) {
            VkInput(
                value = content.value,
                onValueChange = { content.value = it },
                placeholderText = "Placeholder text",
                leftIcon = painterResource(IconsR.drawable.mail_24px),
                rightIcon = painterResource(IconsR.drawable.close_24px),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
