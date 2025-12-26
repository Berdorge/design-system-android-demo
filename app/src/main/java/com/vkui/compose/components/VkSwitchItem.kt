package com.vkui.compose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import com.vkui.theme.VkTheme

@Composable
fun VkSwitchItem(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    enabled: Boolean = true,
    switchAlignment: SwitchAlignment = SwitchAlignment.END,
) {
    @Composable
    fun DrawSwitch() {
        VkSwitch(
            checked = checked,
            onCheckedChange = if (enabled) {
                onCheckedChange
            } else {
                null
            }
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = onCheckedChange
            )
            .padding(
                horizontal = VkTheme.dimens.sizeBasePaddingHorizontal,
                vertical = VkTheme.dimens.spacingSizeL
            )
            .thenIf(!enabled) {
                Modifier.alpha(VkTheme.opacity.opacityDisable)
            }
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(VkTheme.dimens.spacingSizeXl)
    ) {
        if (switchAlignment == SwitchAlignment.START) {
            DrawSwitch()
        }

        Column(
            modifier = Modifier.weight(1F),
            verticalArrangement = Arrangement.spacedBy(VkTheme.dimens.spacingSize2Xs)
        ) {
            if (title != null) {
                VkText(
                    text = title,
                    color = VkTheme.colors.colorTextPrimary,
                    style = VkTheme.typography.fontText
                )
            }
            if (subtitle != null) {
                VkText(
                    text = subtitle,
                    color = VkTheme.colors.colorTextSecondary,
                    style = VkTheme.typography.fontFootnote
                )
            }
        }

        if (switchAlignment == SwitchAlignment.END) {
            DrawSwitch()
        }
    }
}

enum class SwitchAlignment {
    START,
    END
}
