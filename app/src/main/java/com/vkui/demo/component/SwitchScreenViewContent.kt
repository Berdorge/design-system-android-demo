package com.vkui.demo.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.viewinterop.AndroidView
import com.vkui.compose.components.SwitchAlignment
import com.vkui.view.components.ContentBadgeAppearance
import com.vkui.view.components.VkContentBadge
import com.vkui.view.components.VkSwitchItem

@Composable
fun SwitchScreenViewContent(
    title: State<TextFieldValue>,
    subtitle: State<TextFieldValue>,
    isEnabled: State<Boolean>,
    switchAlignment: State<SwitchAlignment>
) {
    VkContentBadge(
        text = "View",
        appearance = ContentBadgeAppearance.Accent
    )
    for (i in 0..2) {
        AndroidView(
            factory = { context ->
                VkSwitchItem(context)
            },
            update = { switchItem ->
                switchItem.setTitle(title.value.text.takeIf { i == 0 || i == 2 })
                switchItem.setSubtitle(subtitle.value.text.takeIf { i == 1 || i == 2 })
                switchItem.isEnabled = isEnabled.value
                switchItem.switchAlignment = when (switchAlignment.value) {
                    SwitchAlignment.START -> VkSwitchItem.SwitchAlignment.START
                    SwitchAlignment.END -> VkSwitchItem.SwitchAlignment.END
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
