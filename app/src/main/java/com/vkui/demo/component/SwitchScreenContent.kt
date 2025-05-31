package com.vkui.demo.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import com.vkui.components.SwitchAlignment
import com.vkui.components.VkInput
import com.vkui.components.VkSelectInput
import com.vkui.components.VkSwitchItem
import com.vkui.components.makeImmutable
import com.vkui.theme.VkTheme

object SwitchScreenContent {
    private val switchAlignmentValues = SwitchAlignment.entries
        .associateWith { alignment ->
            when (alignment) {
                SwitchAlignment.START -> "Start"
                SwitchAlignment.END -> "End"
            }
        }
        .makeImmutable()

    private val title = mutableStateOf(TextFieldValue("Title"))
    private val subtitle = mutableStateOf(TextFieldValue("Subtitle"))
    private val isEnabled = mutableStateOf(true)
    private val switchAlignment = mutableStateOf(SwitchAlignment.END)

    @Composable
    fun Content() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(vertical = VkTheme.dimens.sizeBasePaddingVertical),
            verticalArrangement = Arrangement.spacedBy(VkTheme.dimens.spacingSizeM)
        ) {
            Column(
                modifier = Modifier.weight(1F),
                verticalArrangement = Arrangement.Center
            ) {
                for (i in 0..2) {
                    val title = title.value.text.takeIf { i == 0 || i == 2 }
                    val subtitle = subtitle.value.text.takeIf { i == 1 || i == 2 }
                    val (isChecked, updateChecked) = remember {
                        mutableStateOf(false)
                    }
                    VkSwitchItem(
                        checked = isChecked,
                        title = title,
                        subtitle = subtitle,
                        onCheckedChange = updateChecked,
                        enabled = isEnabled.value,
                        switchAlignment = switchAlignment.value,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            ChooseTitleAndSubtitle()
            ChooseEnabledAndButtonAlignment()
        }
    }

    @Composable
    private fun ChooseTitleAndSubtitle() {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = VkTheme.dimens.spacingSizeM),
            horizontalArrangement = Arrangement.spacedBy(VkTheme.dimens.spacingSizeM)
        ) {
            VkInput(
                modifier = Modifier.weight(1F),
                value = title.value,
                onValueChange = { title.value = it },
                placeholderText = "Title",
            )
            VkInput(
                modifier = Modifier.weight(1F),
                value = subtitle.value,
                onValueChange = { subtitle.value = it },
                placeholderText = "Subtitle",
            )
        }
    }

    @Composable
    private fun ChooseEnabledAndButtonAlignment() {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = VkTheme.dimens.spacingSizeM),
            horizontalArrangement = Arrangement.spacedBy(VkTheme.dimens.spacingSizeM)
        ) {
            VkSwitchItem(
                checked = isEnabled.value,
                title = "Enabled",
                onCheckedChange = { isEnabled.value = it },
                modifier = Modifier.weight(1F)
            )
            VkSelectInput(
                selectedValue = switchAlignment.value,
                values = switchAlignmentValues,
                onValueSelected = { switchAlignment.value = it },
                modifier = Modifier.weight(1F)
            )
        }
    }
}
